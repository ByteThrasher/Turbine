package com.bytethrasher.turbine.request;

import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;

import java.nio.charset.StandardCharsets;

@Builder
public class ApacheHttpClientRequestHandler implements RequestHandler {

    @Override
    public Response doRequest(final String location) {
        // TODO: Do the request here.
        return new Response(200, "hello".getBytes(StandardCharsets.UTF_8));
    }
}
