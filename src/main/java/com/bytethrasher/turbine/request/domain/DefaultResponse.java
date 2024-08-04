package com.bytethrasher.turbine.request.domain;

public record DefaultResponse(

        String location,
        int statusCode,
        byte[] body,
        Header[] headers,
        String contentType,
        String reason
) implements Response {

    @Override
    public int compareTo(final Response o) {
        if (o == null) {
            throw new NullPointerException();
        }

        return this.body.length - o.body().length;
    }
}
