package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;

public interface LocationContainer {

    void registerLocations(final LocationBatch nextBatch);

    /**
     * Returns a location that should be visited on the provided domain. Returns null if there is nothing to visit.
     *
     * @param domain
     * @return
     */
    /*
     * This method returns null instead of Optional because creating a lot of Optional instances is very expensive.
     */
    String grabLocation(String domain);
}
