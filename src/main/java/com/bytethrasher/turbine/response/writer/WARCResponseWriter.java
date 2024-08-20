package com.bytethrasher.turbine.response.writer;

import com.bytethrasher.turbine.request.domain.Header;
import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.MediaType;
import org.netpreserve.jwarc.WarcResponse;
import org.netpreserve.jwarc.WarcWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class WARCResponseWriter implements ResponseWriter {

    // TODO: Add rolling...
    private final WarcWriter writer;

    @Builder
    @SneakyThrows
    public WARCResponseWriter() {
        writer = new WarcWriter(Path.of(System.getProperty("user.dir")).resolve("result.warc"));
    }

    //TODO: Isn't it possible that the main thread shuts down faster than everything is written to file?
    @Override
    @SneakyThrows
    public void writeResponsesFromQueue(final BlockingQueue<Response> queue) {
        while (true) {
            final Response response = queue.take();

            final WarcResponse.Builder warcResponseRecordBuilder = new WarcResponse.Builder(response.location())
                    .date(Instant.now())
                    .body(
                            //TODO: Support reason
                            new HttpResponse.Builder(response.statusCode(), response.reason())
                                    .body(buildMediaType(response.contentType()), response.body())
                                    .build()
                    );

            for (Header header : response.headers()) {
                warcResponseRecordBuilder.addHeader(header.name(), header.value());
            }

            writer.write(warcResponseRecordBuilder.build());
        }
    }

    private MediaType buildMediaType(final String contentType) {
        try {
            return contentType == null ? null : MediaType.parse(contentType);
        } catch (final IllegalArgumentException e) {
            // An IllegalArgumentException is thrown when the media type is not parsable. This happens when the server
            //  on the other end returns garbage.
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
