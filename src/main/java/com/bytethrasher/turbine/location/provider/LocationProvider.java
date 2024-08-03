package com.bytethrasher.turbine.location.provider;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;

public interface LocationProvider {

    LocationBatch provideLocations();
}
