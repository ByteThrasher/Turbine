package com.bytethrasher.turbine.request;

import com.bytethrasher.turbine.request.domain.DefaultHeader;
import com.bytethrasher.turbine.request.domain.DefaultResponse;
import com.bytethrasher.turbine.request.domain.Header;
import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;

public class ApacheHttpClientRequestHandler implements RequestHandler {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
            + " (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246";

    private final HttpClient httpClient;

    @Builder
    public ApacheHttpClientRequestHandler(final String userAgent) {
        final String realUserAgent = userAgent == null ? DEFAULT_USER_AGENT : userAgent;

        httpClient = HttpClientBuilder.create()
                .setUserAgent(realUserAgent)
                .build();
    }

    @Override
    @SneakyThrows //TODO: This SneakyThrows can be very bad here (kill the app)
    public Response doRequest(final String location) {
        try {
            return httpClient.execute(new HttpGet(location), response -> {
                        final Header[] headers = new Header[response.getHeaders().length];

                        for (int i = 0; i < response.getHeaders().length; i++) {
                            final org.apache.hc.core5.http.Header header = response.getHeaders()[i];

                            headers[i] = new DefaultHeader(header.getName(), header.getValue());
                        }

                        return new DefaultResponse(location, response.getCode(),
                                EntityUtils.toByteArray(response.getEntity()), headers,
                                response.getEntity().getContentType(), response.getReasonPhrase());
                    }
            );
        } catch (final NoHttpResponseException e) {
            // The server failed to respond with anything meaningful. Let's return a poison pill so we doesn't process
            // any of the other locations from this domain.
            // TODO: Why null? Other than it's faster to return than throwing an exception.
            return null;
        }
    }
}
