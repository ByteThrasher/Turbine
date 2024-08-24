package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.process.CrawlingProcess;
import com.bytethrasher.turbine.request.RequestHandlerFactory;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;

@Slf4j
public class FixedSizeProcessStarter implements ProcessStarter {

    private final int crawlDelay;
    private final int threadLimit;

    private final Semaphore semaphore;

    @Builder
    public FixedSizeProcessStarter(int threadLimit, int crawlDelay) {
        this.threadLimit = threadLimit == 0 ? 10 : threadLimit;
        this.crawlDelay = crawlDelay == 0 ? 1000 : crawlDelay;

        semaphore = new Semaphore(threadLimit);
    }

    @Override
    public void waitUntilAbleToStart() {
        semaphore.acquireUninterruptibly();
        semaphore.release();
    }

    @Override
    public void waitUntilFinish() {
        log.info("Waiting for processes to finish.");

        semaphore.acquireUninterruptibly(threadLimit);
    }

    @Override
    public void startProcess(final String domain, final LocationContainer locationContainer,
            final RequestHandlerFactory requestHandlerFactory, final ResponseHandler responseHandler,
            final BlockingQueue<Response> queue) {
        log.info("Starting process to crawl domain: {}.", domain);

        semaphore.acquireUninterruptibly();

        Thread.ofVirtual()
                .name("turbine-crawler-" + domain)
                // TODO: I think the response handler should come from outside as well...
                .start(() -> {
                    new CrawlingProcess(domain, crawlDelay, locationContainer, requestHandlerFactory,
                            responseHandler, queue).run();

                    semaphore.release();
                });
    }
}
