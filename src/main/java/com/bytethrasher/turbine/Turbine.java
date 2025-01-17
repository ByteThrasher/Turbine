package com.bytethrasher.turbine;

import com.bytethrasher.turbine.location.container.DefaultLocationContainer;
import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.location.provider.FileBasedLocationProvider;
import com.bytethrasher.turbine.location.provider.LocationProvider;
import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;
import com.bytethrasher.turbine.process.starter.FixedSizeProcessStarter;
import com.bytethrasher.turbine.process.starter.ProcessStarter;
import com.bytethrasher.turbine.request.RequestHandlerFactory;
import com.bytethrasher.turbine.request.ApacheHttpClientRequestHandlerFactory;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import com.bytethrasher.turbine.response.writer.ResponseWriter;
import com.bytethrasher.turbine.response.writer.WARCResponseWriter;
import com.bytethrasher.turbine.util.queue.BoundedPriorityBlockingQueue;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

/**
 * The main class that can be used to create and start the crawler.
 */
@Slf4j
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

    @Builder.Default
    private final RequestHandlerFactory requestHandlerFactory = ApacheHttpClientRequestHandlerFactory.builder()
            .build();

    @Builder.Default
    private final ResponseHandler responseHandler = ResponseHandler.builder()
            .build();

    @Builder.Default
    private final ResponseWriter responseWriter = WARCResponseWriter.builder()
            .build();

    @Builder.Default
    // TODO: queue what? Should be more specific.
    private final int queueCapacity = 100;

    @SneakyThrows
    public void start() {
        try {
            log.info("Starting the turbine engine.");

            final BoundedPriorityBlockingQueue<Response> queue = new BoundedPriorityBlockingQueue<>(queueCapacity);

            Thread.startVirtualThread(() -> responseWriter.writeResponsesFromQueue(queue));

            Thread locationAcquiringThread = Thread.ofVirtual()
                    .name("turbine-writer-thread")
                    .start(() -> {
                        while (true) {
                            final DefaultLocationBatch locationBatch = locationProvider.provideLocations();

                            if (locationBatch == null) {
                                break;
                            } else {
                                // The location container will block the main thread if it overflows.
                                locationContainer.registerLocations(locationBatch);
                            }
                        }
                    });

            while (true) {
                if (!locationAcquiringThread.isAlive() && locationContainer.isEmpty()) {
                    processStarter.waitUntilFinish();

                    log.info("Crawl finished! No more locations to process.");
                    break;
                } else {
                    processStarter.waitUntilAbleToStart();

                    final String domain = locationContainer.allocateDomain();

                    if (domain != null) {
                        // TODO: Instead of passing the queue around, there should be an abstraction above it.
                        processStarter.startProcess(domain, locationContainer, requestHandlerFactory,
                                responseHandler, queue);
                    }
                }
            }
        } finally {
            responseWriter.close();
        }
    }
}
