package com.bytethrasher.turbine.response.writer;

import com.bytethrasher.turbine.request.domain.Response;

import java.io.Closeable;
import java.util.concurrent.BlockingQueue;

public interface ResponseWriter extends Closeable {

    void writeResponsesFromQueue(BlockingQueue<Response> queue);
}
