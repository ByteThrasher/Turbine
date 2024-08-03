package com.bytethrasher.turbine.request.domain;

public record Response(

        int statusCode,
        byte[] content
        // TODO: Headers, etc.
) implements Comparable<Response> {

    @Override
    public int compareTo(final Response o) {
        // TODO: handle null
        return this.content.length - o.content.length;
    }
}
