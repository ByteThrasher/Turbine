package com.bytethrasher.turbine.process;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.request.RequestHandler;
import com.bytethrasher.turbine.request.RequestHandlerFactory;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
public class CrawlingProcess implements Runnable {

    private final String domain;
    private final int crawlDelay;
    private final LocationContainer locationContainer;
    private final RequestHandler requestHandler;
    private final ResponseHandler responseHandler;
    private final BlockingQueue<Response> queue;

    public CrawlingProcess(final String domain, final int crawlDelay, final LocationContainer locationContainer,
            final RequestHandlerFactory requestHandlerFactory, final ResponseHandler responseHandler,
            final BlockingQueue<Response> queue) {
        this.domain = domain;
        this.crawlDelay = crawlDelay;
        this.locationContainer = locationContainer;
        this.requestHandler = requestHandlerFactory.newRequestHandler();
        this.responseHandler = responseHandler;
        this.queue = queue;
    }

    @Override
    @SneakyThrows
    public void run() {
        try {
            String location = locationContainer.grabLocation(domain);

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
            requestHandler.close();
        }
    }
}
