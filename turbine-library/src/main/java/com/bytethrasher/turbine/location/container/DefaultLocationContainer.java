package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
import lombok.Builder;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Builder
public class DefaultLocationContainer implements LocationContainer {

    private final Map<String, List<String>> locations = new HashMap<>();

    private final Semaphore semaphore = new Semaphore(0);

    @Builder.Default
    private int actualLocationsUnderProcessing = 0;

    @Builder.Default
    private int maximumLocationsUnderProcessing = 10000;

    @Override
    @SneakyThrows
    public void registerLocations(final LocationBatch nextBatch) {
        if (locations.containsKey(nextBatch.domain())) {
            locations.get(nextBatch.domain()).addAll(nextBatch.locations());
        } else {
            // The location provider should return a modifiable set by design!
            locations.put(nextBatch.domain(), nextBatch.locations());
        }

        actualLocationsUnderProcessing += nextBatch.locations().size();

        if (actualLocationsUnderProcessing > maximumLocationsUnderProcessing) {
            semaphore.acquire();
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

        if (actualLocationsUnderProcessing < maximumLocationsUnderProcessing) {
            semaphore.release();
        }

        return location;
    }
}
