package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.process.CrawlingProcess;
import com.bytethrasher.turbine.request.RequestHandler;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

@Slf4j
@Builder
public class FixedSizeProcessStarter implements ProcessStarter {

    @Builder.Default
    private final int threadLimit = 10;

    @Builder.Default
    private final int crawlDelay = 1000;

    private final List<Thread> threads = new LinkedList<>();

    @Override
    public boolean atProcessLimit() {
        // Remove dead threads
        threads.removeIf(thread -> !thread.isAlive());

        return threads.size() >= threadLimit;
    }

    @Override
    @SneakyThrows
    public void waitUntilFinish() {
        log.info("Waiting for processes to finish.");

        while (!threads.isEmpty()) {
            threads.removeIf(thread -> !thread.isAlive());

            if (!threads.isEmpty()) {
                threads.getFirst().join();
            }
        }
    }

    @Override
    public void startProcess(final String domain, final LocationContainer locationContainer,
            final RequestHandler requestHandler, final ResponseHandler responseHandler,
            final BlockingQueue<Response> queue) {
        log.info("Starting process to crawl domain: {}.", domain);

        threads.add(
                Thread.ofVirtual()
                        .name(domain)
                        // TODO: I think the response handler should come from outside as well...
                        .start(new CrawlingProcess(domain, crawlDelay, locationContainer, requestHandler, responseHandler, queue))
        );
    }
}
