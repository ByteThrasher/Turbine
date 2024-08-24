package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.process.CrawlingProcess;
import com.bytethrasher.turbine.request.RequestHandlerFactory;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import oshi.SystemInfo;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ResourceBasedProcessStarter implements ProcessStarter {

    private final SystemInfo systemInfo = new SystemInfo();

    private final int crawlDelay;
    private final int targetCPUUtilisation;

    private final ConcurrentHashMap<String, Thread> activeThreads = new ConcurrentHashMap<>();

    private int lastLoadPercentage = 0;

    @Builder
    public ResourceBasedProcessStarter(final int targetCPUUtilisation, final int crawlDelay) {
        this.crawlDelay = crawlDelay == 0 ? 1000 : crawlDelay;

        if (targetCPUUtilisation > 100 || targetCPUUtilisation < 0) {
            throw new IllegalArgumentException("The targetCPUUtilisation must be between 0 and 100.");
        }

        this.targetCPUUtilisation = targetCPUUtilisation > 0 ? targetCPUUtilisation : 80;
    }

    @Override
    public void waitUntilAbleToStart() {
        while (true) {
            final double load = getSystemCpuLoad();

            final int loadPercentage = (int) Math.round(load * 100);
            if (loadPercentage > 0) {
                lastLoadPercentage = loadPercentage;
            }

            if (lastLoadPercentage < targetCPUUtilisation) {
                return;
            }
        }
    }

    @Override
    @SneakyThrows
    public void waitUntilFinish() {
        // TODO: the thread caching needs more testing and improvements. This is a very naive solution.
        while (true) {
            if (activeThreads.isEmpty()) {
                return;
            } else {
                Thread.sleep(50);
            }
        }
    }

    @Override
    public void startProcess(final String domain, final LocationContainer locationContainer,
            final RequestHandlerFactory requestHandlerFactory, final ResponseHandler responseHandler,
            final BlockingQueue<Response> queue) {
        log.info("Starting process to crawl domain: {}.", domain);

        // TODO: the thread caching needs more testing and improvements. This is a very naive solution.
        final Thread thread = Thread.ofVirtual()
                .name("turbine-crawler-" + domain)
                // TODO: I think the response handler should come from outside as well...
                .start(() -> {
                    new CrawlingProcess(domain, crawlDelay, locationContainer, requestHandlerFactory,
                            responseHandler, queue).run();

                    activeThreads.remove("turbine-crawler-" + domain);
                });

        activeThreads.put(thread.getName(), thread);
    }

    @SneakyThrows
    private double getSystemCpuLoad() {
        final long[] oldTicks = systemInfo.getHardware().getProcessor().getSystemCpuLoadTicks();

        //Thread.sleep(0, 5000);

        return systemInfo.getHardware().getProcessor().getSystemCpuLoadBetweenTicks(oldTicks);
    }
}
