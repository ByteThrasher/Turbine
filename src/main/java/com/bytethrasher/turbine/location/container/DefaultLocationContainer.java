package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;
import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
import com.bytethrasher.turbine.util.collection.PartitionList;
import lombok.Builder;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Semaphore;

/**
 * This class holds the locations (urls mostly) that should be visited by the crawling threads. It supports data access
 * from multiple threads because that's required by the other parts of Turbine. Usually the new locations are being fed
 * to the container by a dedicated thread while it's contents are being consumed by many crawler threads simultaneously.
 */
public class DefaultLocationContainer implements LocationContainer {

    private final Map<String, List<String>> locations = new HashMap<>();
    private final Set<String> underProcessingDomains = new TreeSet<>();
    private final List<String> availableDomains = new LinkedList<>();

    private final int maximumLocationsUnderProcessing;
    private final Semaphore semaphore;

    @Builder
    public DefaultLocationContainer(final int maximumLocationsUnderProcessing) {
        this.maximumLocationsUnderProcessing = maximumLocationsUnderProcessing == 0
                ? 10000 : maximumLocationsUnderProcessing;

        semaphore = new Semaphore(this.maximumLocationsUnderProcessing);
    }

    @Override
    @SneakyThrows
    public void registerLocations(final LocationBatch nextBatch) {
        if (nextBatch.locations().size() > maximumLocationsUnderProcessing) {
            final int partialBatchSize = Math.max(maximumLocationsUnderProcessing / 2, 1);
            final List<List<String>> partialLocationBatches = new PartitionList<>(
                    nextBatch.locations(), partialBatchSize);

            partialLocationBatches.forEach(list ->
                    registerLocations(new DefaultLocationBatch(nextBatch.domain(), list)));
        } else {
            // We can still need to hold the locations in the memory if we have no place for them,
            // but at least the thread is blocked from acquiring more.
            semaphore.acquire(nextBatch.locations().size());

            synchronized (locations) {
                if (locations.containsKey(nextBatch.domain())) {
                    locations.get(nextBatch.domain()).addAll(nextBatch.locations());
                } else {
                    locations.put(nextBatch.domain(), new LinkedList<>(nextBatch.locations()));

                    synchronized (underProcessingDomains) {
                        if (!underProcessingDomains.contains(nextBatch.domain())) {
                            synchronized (availableDomains) {
                                availableDomains.add(nextBatch.domain());
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public String grabLocation(final String domain) {
        synchronized (locations) {
            if (!locations.containsKey(domain)) {
                return null;
            }

            final String location = locations.get(domain).removeFirst();

            if (locations.get(domain).isEmpty()) {
                locations.remove(domain);
            }

            semaphore.release(1);

            return location;
        }
    }

    @Override
    public String allocateDomain() {
        synchronized (availableDomains) {
            if (availableDomains.isEmpty()) {
                return null;
            }

            final String allocatedDomain = availableDomains.removeLast();

            synchronized (underProcessingDomains) {
                underProcessingDomains.add(allocatedDomain);
            }

            return allocatedDomain;
        }
    }

    @Override
    public void deallocateDomain(final String domain) {
        synchronized (underProcessingDomains) {
            underProcessingDomains.remove(domain);
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (locations) {
            return locations.isEmpty();
        }
    }
}
