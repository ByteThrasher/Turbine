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

        try {
            do {
                log.info("Crawling location: {}.", location);

                final Response response = requestHandler.doRequest(location);

                if (response != null) {
                    responseHandler.handleResponse(response, queue);

                    Thread.sleep(crawlDelay);

                    location = locationContainer.grabLocation(domain);
                } else {
                    //TODO: It would be good if we could cache the bad domains for at least a hour or smthing

                    // The domain is an invalid one. Either it doesn't exist or a server doesn't respond with anything
                    // meaningful on hte other end. Let's drop all the locations we intended to crawl on it and just
                    // carry on.
                    locationContainer.dropDomain(domain);
                    break;
                }
            } while (location != null);
        } finally {
            // No matter what, the domain should be deallocated. Otherwise we will run into deadlocks.
            locationContainer.deallocateDomain(domain);
        }
    }
}
