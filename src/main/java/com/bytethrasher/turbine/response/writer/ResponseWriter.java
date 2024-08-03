package com.bytethrasher.turbine.response.writer;

import com.bytethrasher.turbine.request.domain.Response;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public interface ResponseWriter {

    void writeResponsesFromQueue(BlockingQueue<Response> queue);
}
