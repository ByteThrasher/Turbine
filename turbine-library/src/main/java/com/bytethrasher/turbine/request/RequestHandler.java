package com.bytethrasher.turbine.request;

import com.bytethrasher.turbine.request.domain.Response;

public interface RequestHandler {

    Response doRequest(String location);
}
