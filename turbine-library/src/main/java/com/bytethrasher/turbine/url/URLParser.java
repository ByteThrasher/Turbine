package com.bytethrasher.turbine.url;

import lombok.SneakyThrows;

import java.net.URI;

public class URLParser {

    // TODO: something faster, we doesn't need to parse the whole url just the domain
    // TODO: properly handle invalid urls
    @SneakyThrows
    public String parseDomain(final String location) {
        return new URI(location).getHost();
    }
}
