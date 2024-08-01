package com.bytethrasher.turbine;

import com.bytethrasher.turbine.location.container.DefaultLocationContainer;
import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.location.provider.FileBasedLocationProvider;
import com.bytethrasher.turbine.location.provider.LocationProvider;
import com.bytethrasher.turbine.location.provider.domain.LocationBatch;
import com.bytethrasher.turbine.process.starter.FixedSizeProcessStarter;
import com.bytethrasher.turbine.process.starter.ProcessStarter;
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
            .locationPath(Path.of(System.getProperty("user.dir")).resolve("locations.txt"))
            .build();

    @Builder.Default
    private final LocationContainer locationContainer = DefaultLocationContainer.builder()
            .build();

    @Builder.Default
    private final ProcessStarter processStarter = FixedSizeProcessStarter.builder()
            .build();

    @SneakyThrows
    public void start() {
        // TODO: while not stopped/interrupted
        while (true) {
            final LocationBatch locationBatch = locationProvider.provideLocations();

            if (locationBatch == null) {
                processStarter.waitUntilFinish();
                break;
            }

            // The location container will block the main thread if it overflows.
            locationContainer.registerLocations(locationBatch);

            if (!processStarter.atProcessLimit()) {
                final String domain = locationContainer.grabDomain();

                if (domain != null) {
                    processStarter.startProcess(domain, locationContainer);
                }
            }
        }
    }
}
