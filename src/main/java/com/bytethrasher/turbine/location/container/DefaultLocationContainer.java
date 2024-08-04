package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
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
 * This class holds the urls that should be visited by the crawling threads.
 * <p>
 * The {@link #grabLocation(String)} method for any given domain should only be called by the same thread every time.
 * Otherwise, a {@link java.util.ConcurrentModificationException} could occur.
 * <p>
 * Same stands for the {@link #registerLocations(LocationBatch)} method. It should only be called by the same thread or
 * a deadlock might occur.
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
            // TODO: Would be great if we could register locations one by one without creating an unnecessary list.
            for (String location : nextBatch.locations()) {
                final List<String> newList = new LinkedList<>();

                newList.add(location);

                registerLocations(new LocationBatch(nextBatch.domain(), newList));
            }
        } else {
            // We can still need to hold the locations in the memory if we have no place for them,
            // but at least the thread is blocked from acquiring more.
            semaphore.acquire(nextBatch.locations().size());

            if (locations.containsKey(nextBatch.domain())) {
                locations.get(nextBatch.domain()).addAll(nextBatch.locations());
            } else {
                // TODO: Why thoug? We should just create our won set and copy everything...
                // The location provider should return a modifiable set by design!
                locations.put(nextBatch.domain(), nextBatch.locations());

                if (!underProcessingDomains.contains(nextBatch.domain())) {
                    availableDomains.add(nextBatch.domain());
                }
            }
        }
    }

    @Override
    public String grabLocation(String domain) {
        if (!locations.containsKey(domain)) {
            return null;
        }

        String location = locations.get(domain).removeFirst();

        if (locations.get(domain).isEmpty()) {
            locations.remove(domain);
        }

        semaphore.release(1);

        return location;
    }

    @Override
    public String allocateDomain() {
        if (availableDomains.isEmpty()) {
            return null;
        }

        String allocatedDomain = availableDomains.removeLast();
        underProcessingDomains.add(allocatedDomain);
        return allocatedDomain;
    }

    @Override
    public void deallocateDomain(final String domain) {
        //TODO: This can cause some mess because it is being called by the workers. We should lock on the
        // underProcessingDomains I guess.
        underProcessingDomains.remove(domain);
    }

    @Override
    public boolean isEmpty() {
        return locations.isEmpty();
    }
}
