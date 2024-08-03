package com.bytethrasher.turbine.response.writer;

import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Builder
public class WARCResponseWriter implements ResponseWriter {

    @Override
    @SneakyThrows
    public void writeResponsesFromQueue(final BlockingQueue<Response> queue) {
        while (true) {
            final Response response = queue.take();

            //TODO: write properly
            log.info(response.toString());
        }
    }
}
