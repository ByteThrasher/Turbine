package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;
import com.bytethrasher.turbine.request.RequestHandlerFactory;
import com.bytethrasher.turbine.request.domain.Response;
import com.bytethrasher.turbine.response.ResponseHandler;

import java.util.concurrent.BlockingQueue;

public interface ProcessStarter {

    void waitUntilAbleToStart();

    void waitUntilFinish();

    void startProcess(String domain, LocationContainer locationContainer, RequestHandlerFactory requestHandlerFactory,
            ResponseHandler responseHandler, BlockingQueue<Response> queue);
}
