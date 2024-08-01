package com.bytethrasher.turbine.process;

import com.bytethrasher.turbine.location.container.LocationContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CrawlingProcess implements Runnable {

    private final String domain;
    private final int crawlDelay;
    private final LocationContainer locationContainer;

    @Override
    public void run() {
        String location = locationContainer.grabLocation(domain);

        do {
            log.info("Crawling location: {}.", location);

            try {
                Thread.sleep(crawlDelay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            location = locationContainer.grabLocation(domain);
        } while (location != null);
    }
}
