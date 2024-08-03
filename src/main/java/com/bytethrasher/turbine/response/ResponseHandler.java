package com.bytethrasher.turbine.response;

import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.util.concurrent.BlockingQueue;

@Builder
@RequiredArgsConstructor
public class ResponseHandler {

    @SneakyThrows
    public void handleResponse(final Response response, final BlockingQueue<Response> queue) {
        // TODO: Add filtering etc

        // Write the queue here. Another thread will read from it on the other side and writes the
        // responses to somewhere.
        queue.put(response);
    }
}
