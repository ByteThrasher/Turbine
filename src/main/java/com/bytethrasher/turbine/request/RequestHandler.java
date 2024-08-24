package com.bytethrasher.turbine.request;

import com.bytethrasher.turbine.request.domain.Response;

import java.io.Closeable;

public interface RequestHandler extends Closeable {

    Response doRequest(String location);
}
