package com.bytethrasher.turbine.location.provider;

import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;

public interface LocationProvider {

    DefaultLocationBatch provideLocations();
}
