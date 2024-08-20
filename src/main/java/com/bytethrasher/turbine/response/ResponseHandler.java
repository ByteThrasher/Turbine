package com.bytethrasher.turbine.response;

import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;

@Slf4j
@Builder
@RequiredArgsConstructor
public class ResponseHandler {

    @SneakyThrows
    public void handleResponse(final Response response, final BlockingQueue<Response> queue) {
        log.debug("Handling response for location: {}.", response.location());

        // TODO: Add filtering etc

        // Write the queue here. Another thread will read from it on the other side and writes the
        // responses to somewhere.
        queue.put(response);
    }
}
