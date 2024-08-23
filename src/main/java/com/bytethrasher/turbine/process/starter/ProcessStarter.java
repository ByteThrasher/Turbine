package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.request.RequestHandler;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;

import java.util.concurrent.BlockingQueue;

public interface ProcessStarter {

    void waitForFreeSpace();

    void waitUntilFinish();

    void startProcess(String domain, LocationContainer locationContainer, RequestHandler requestHandler,
            ResponseHandler responseHandler, BlockingQueue<Response> queue);
}
