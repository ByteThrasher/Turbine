package com.bytethrasher.turbine.process;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.request.RequestHandler;
import com.bytethrasher.turbine.request.domain.DefaultResponse;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
@RequiredArgsConstructor
public class CrawlingProcess implements Runnable {

    private final String domain;
    private final int crawlDelay;
    private final LocationContainer locationContainer;
    private final RequestHandler requestHandler;
    private final ResponseHandler responseHandler;
    private final BlockingQueue<Response> queue;

    @Override
    @SneakyThrows
    public void run() {
        String location = locationContainer.grabLocation(domain);

        do {
            log.info("Crawling location: {}.", location);

            responseHandler.handleResponse(requestHandler.doRequest(location), queue);

            Thread.sleep(crawlDelay);

            location = locationContainer.grabLocation(domain);
        } while (location != null);

        locationContainer.deallocateDomain(domain);
    }
}
