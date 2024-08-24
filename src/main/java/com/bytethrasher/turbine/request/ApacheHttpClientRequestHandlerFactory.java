package com.bytethrasher.turbine.request;

import lombok.Builder;

@Builder
public class ApacheHttpClientRequestHandlerFactory implements RequestHandlerFactory {

    // TODO: This should be a parameter somewhere.
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            + " (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";

    @Override
    public RequestHandler newRequestHandler() {
        return new ApacheHttpClientRequestHandler(DEFAULT_USER_AGENT);
    }
}
