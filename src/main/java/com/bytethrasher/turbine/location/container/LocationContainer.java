package com.bytethrasher.turbine.location.container;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;

/**
 * A {@link LocationContainer} contains the locations (usually urls) that the crawling threads should visit. It is also
 * responsible to limit the number of locations available in the container by blocking the location providers before
 * they would start to overproduce.
 */
public interface LocationContainer {

    /**
     * Register the provided locations under the provided domain for crawling. If the number of provided locations
     * is more than the container has free space for then it will block the providing thread until sufficient space is
     * become available.
     *
     * @param nextBatch the next batch of locations to register in the container
     */
    void registerLocations(final LocationBatch nextBatch);

    /**
     * Returns a location that should be visited on the provided domain. Returns null if there is nothing to visit.
     *
     * @param domain the domain to get a location for
     * @return a location on the domain that should be visited
     */
    String grabLocation(String domain);

    String allocateDomain();

    void deallocateDomain(String domain);

    void dropDomain(String domain);

    boolean isEmpty();
}
