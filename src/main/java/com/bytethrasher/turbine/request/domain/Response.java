package com.bytethrasher.turbine.request.domain;

public interface Response extends Comparable<Response> {

    String location();

    int statusCode();

    byte[] body();

    Header[] headers();

    String contentType();

    String reason();
}
