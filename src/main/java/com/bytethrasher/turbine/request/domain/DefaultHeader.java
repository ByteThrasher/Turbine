package com.bytethrasher.turbine.request.domain;

public record DefaultHeader(

        String name,
        String value
) implements Header {
}
