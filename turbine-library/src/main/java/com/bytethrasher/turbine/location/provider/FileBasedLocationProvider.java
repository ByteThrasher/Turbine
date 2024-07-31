package com.bytethrasher.turbine.location.provider;

import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
import lombok.Builder;
import lombok.NonNull;

import java.nio.file.Path;

@Builder
public class FileBasedLocationProvider implements LocationProvider {

    @NonNull
    private final Path locationFile;

    @Override
    public LocationBatch provideLocations() {
        // TODO
        return null;
    }
}
