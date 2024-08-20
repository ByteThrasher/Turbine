package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;
import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
import com.bytethrasher.turbine.util.collection.PartitionList;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

/**
 * This class holds the locations (urls mostly) that should be visited by the crawling threads. It supports data access
 * from multiple threads because that's required by the other parts of Turbine. Usually the new locations are being fed
 * to the container by a dedicated thread while it's contents are being consumed by many crawler threads simultaneously.
 */
@Slf4j
public class DefaultLocationContainer implements LocationContainer {

    private final ConcurrentHashMap<String, DomainWorkUnit> locations = new ConcurrentHashMap<>();

    private final int maximumLocationsUnderProcessing;
    private final Semaphore semaphore;

    @Builder
    public DefaultLocationContainer(final int maximumLocationsWaitingForProcessing) {
        this.maximumLocationsUnderProcessing = maximumLocationsWaitingForProcessing == 0
                ? 10000 : maximumLocationsWaitingForProcessing;

        semaphore = new Semaphore(this.maximumLocationsUnderProcessing);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The method supports the registering of more than the {@link #maximumLocationsUnderProcessing} of locations in
     * one go. In this case the provided locations will be made available in smaller batches.
     *
     * @param nextBatch the next batch of locations to register in the container
     */
    @Override
    @SneakyThrows
    public void registerLocations(final LocationBatch nextBatch) {
        if (nextBatch.locations().size() > maximumLocationsUnderProcessing) {
            log.debug("Too big batch to register! Splitting it up.");

            final int partialBatchSize = Math.max(maximumLocationsUnderProcessing / 2, 1);
            final List<List<String>> partialLocationBatches = new PartitionList<>(
                    nextBatch.locations(), partialBatchSize);

            partialLocationBatches.forEach(list ->
                    registerLocations(new DefaultLocationBatch(nextBatch.domain(), list)));
        } else {
            log.debug("Registering batch for domain: {}.", nextBatch.domain());

            // We can still need to hold the locations in the memory if we have no place for them,
            // but at least the thread is blocked from acquiring more.
            semaphore.acquire(nextBatch.locations().size());

            locations.merge(nextBatch.domain(), new DomainWorkUnit(nextBatch.domain(), nextBatch.locations()),
                    (oldValue, newValue) -> {
                        oldValue.locations.addAll(newValue.locations);

                        return oldValue;
                    });
        }
    }

    @Override
    public String grabLocation(final String domain) {
        synchronized (locations) {
            log.debug("Grabbing location for domain: {}.", domain);

            if (!locations.containsKey(domain)) {
                return null;
            }

            final String location = locations.get(domain).locations.removeFirst();

            if (locations.get(domain).locations.isEmpty()) {
                locations.remove(domain);
            }

            semaphore.release(1);

            return location;
        }
    }

    @Override
    public String allocateDomain() {
        log.debug("Allocating domain.");

        synchronized (locations) {
            if (locations.isEmpty()) {
                return null;
            }

            for (DomainWorkUnit domainWorkUnit : locations.values()) {
                if (!domainWorkUnit.underProcessing) {
                    domainWorkUnit.underProcessing = true;

                    return domainWorkUnit.domain;
                }
            }

            // No unallocated domain was found.
            return null;
        }
    }

    @Override
    public void dropDomain(final String domain) {
        log.debug("Dropping domain: {}.", domain);

        locations.entrySet()
                .removeIf(entry -> entry.getKey().equals(domain));
    }

    @Override
    public boolean isEmpty() {
        synchronized (locations) {
            return locations.isEmpty();
        }
    }

    private static class DomainWorkUnit {

        private final String domain;
        private final List<String> locations = new LinkedList<>();

        public DomainWorkUnit(final String domain, final List<String> locations) {
            this.domain = domain;

            this.locations.addAll(locations);
        }

        private boolean underProcessing = false;
    }
}
