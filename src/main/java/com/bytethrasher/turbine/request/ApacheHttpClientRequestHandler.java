package com.bytethrasher.turbine.request;

import com.bytethrasher.turbine.request.domain.DefaultHeader;
import com.bytethrasher.turbine.request.domain.DefaultResponse;
import com.bytethrasher.turbine.request.domain.Header;
import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class ApacheHttpClientRequestHandler implements RequestHandler {

    private final CloseableHttpClient httpClient;

    @Builder
    @SneakyThrows
    public ApacheHttpClientRequestHandler(final String userAgent) {
        final TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
        final SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();
        final SSLConnectionSocketFactory sslsf =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        final Registry<ConnectionSocketFactory> socketFactoryRegistry =
                RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("https", sslsf)
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .build();

        // We are using a basic connection manager because each domain gets its own RequestHandler.
        final BasicHttpClientConnectionManager connectionManager =
                new BasicHttpClientConnectionManager(socketFactoryRegistry);

        // TODO: Configure maximum redirect.
        httpClient = HttpClientBuilder.create()
                // TODO: The connection manager above is created to allow connections to unverified SSL hosts which
                //  might not be the desired behaviour by every caller. We need to add a flag like
                //  "thrustAllCertificates" or something similar.
                //  See: https://www.baeldung.com/httpclient-ssl#ssl_config
                .setConnectionManager(connectionManager)
                .setUserAgent(userAgent)
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
        } catch (final SocketTimeoutException | SSLHandshakeException | ClientProtocolException e) {
            // TODO: retry or return failed response
            return null;
        } catch (final NoHttpResponseException | SocketException | UnknownHostException e) {
            // The server failed to respond with anything meaningful. Let's return a poison pill so we doesn't process
            // any of the other locations from this domain.
            // TODO: Why null? Other than it's faster to return than throwing an exception. We need to return with a
            //  proper exception though.
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }
}
