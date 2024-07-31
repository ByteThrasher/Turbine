package com.bytethrasher.turbine;

import com.bytethrasher.turbine.location.container.DefaultLocationContainer;
import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.location.provider.FileBasedLocationProvider;
import com.bytethrasher.turbine.location.provider.LocationProvider;
import lombok.Builder;
import lombok.SneakyThrows;

import java.nio.file.Path;

/**
 * The main class that can be used to create and start the crawler.
 */
@Builder
public class Turbine {

    @Builder.Default
    private final LocationProvider locationProvider = FileBasedLocationProvider.builder()
            .locationFile(Path.of("./locations.txt"))
            .build();

    @Builder.Default
    private final LocationContainer locationContainer = DefaultLocationContainer.builder()
            .build();

    @SneakyThrows
    public void start() {
        // TODO: while not stopped/interrupted
        while (true) {
            locationContainer.registerLocations(locationProvider.provideLocations());

            //TODO: Start new threads to process the locations
        }
    }
}
