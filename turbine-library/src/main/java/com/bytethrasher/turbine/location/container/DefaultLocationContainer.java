package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultLocationContainer implements LocationContainer {

    private final Map<String, List<String>> locations = new HashMap<>();

    // TODO: This should be configurable.
    private int actualLocationsUnderProcessing = 0;

    // TODO: This should be configurable.
    private int maximumLocationsUnderProcessing = 10000;

    @Override
    public void registerLocations(final LocationBatch nextBatch) {
        if (locations.containsKey(nextBatch.domain())) {
            locations.get(nextBatch.domain()).addAll(nextBatch.locations());
        } else {
            // The location provider should return a modifiable set by design!
            locations.put(nextBatch.domain(), nextBatch.locations());
        }

        actualLocationsUnderProcessing += nextBatch.locations().size();
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

        return location;
    }

    // TODO: Instead of using Thread.sleep we should block until we have free space.
    @Override
    public boolean hasFreeSpace() {
        return actualLocationsUnderProcessing < maximumLocationsUnderProcessing;
    }
}
