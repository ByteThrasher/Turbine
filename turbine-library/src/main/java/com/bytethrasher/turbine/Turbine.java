package com.bytethrasher.turbine;

import com.bytethrasher.turbine.location.container.DefaultLocationContainer;
import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.location.provider.FileBasedLocationProvider;
import com.bytethrasher.turbine.location.provider.LocationProvider;
import lombok.Builder;
import lombok.SneakyThrows;

/**
 * The main class that can be used to create and start the crawler.
 */
@Builder
public class Turbine {

    @Builder.Default
    private final LocationProvider locationProvider = new FileBasedLocationProvider();
    @Builder.Default
    private final LocationContainer locationContainer = new DefaultLocationContainer();

    @SneakyThrows
    public void start() {
        // TODO: while not stopped/interrupted
        while (true) {
            if (!locationContainer.hasFreeSpace()) {
                Thread.sleep(1000);

                continue;
            }

            locationContainer.registerLocations(locationProvider.provideLocations());

            // TODO: start new thread if needed
            Thread.sleep(1000);
        }
    }
}
