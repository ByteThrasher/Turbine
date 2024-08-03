package com.bytethrasher.turbine.response.writer;

import com.bytethrasher.turbine.request.domain.Response;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.netpreserve.jwarc.HttpResponse;
import org.netpreserve.jwarc.MediaType;
import org.netpreserve.jwarc.WarcResponse;
import org.netpreserve.jwarc.WarcWriter;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class WARCResponseWriter implements ResponseWriter {

    // TODO: Add rolling...
    private final WarcWriter writer;

    @Builder
    @SneakyThrows
    public WARCResponseWriter() {
        writer = new WarcWriter(System.out);
    }

    //TODO: Isn't it possible that the main thread shuts down faster than everything is written to file?
    @Override
    @SneakyThrows
    public void writeResponsesFromQueue(final BlockingQueue<Response> queue) {
        while (true) {
            final Response response = queue.take();

            // TODO: Save request as well

            WarcResponse warcResponseRecord = new WarcResponse.Builder("asd")
                    .date(Instant.now())
                    .body(
                            //TODO: Support reason
                            new HttpResponse.Builder(response.statusCode(), "asd")
                                    //TODO: Support media type
                                    .body(MediaType.parse("application/html"), response.content())
                                    .build()
                    )
                    // TODO concurrentTo
                    //.concurrentTo(request.id())
                    .build();
            writer.write(warcResponseRecord);
        }
    }
}
