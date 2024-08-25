package com.bytethrasher.turbine.url;

import lombok.SneakyThrows;

import java.util.Locale;

public class URLParser {

    // TODO: instead of domains, we should parse the base domains (www.stackoverflow.com -> stackoverflow.com) but be
    //  careful with endings like amazon.co.uk.
    // TODO: ban localhost, 127.0.0.1 and internal IP addresses.
    @SneakyThrows
    public String parseDomain(final String location) {
        String target = location.strip().toLowerCase(Locale.ENGLISH);

        if (target.startsWith("http://")) {
            target = target.substring(7);
        } else if (target.startsWith("https://")) {
            target = target.substring(8);
        } else {
            return null;
        }

        boolean skipNextChars = false;

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            char character = target.charAt(i);

            if (character == '/') {
                return safetyCheckDomain(result.toString());
            } else if (character == ':') {
                skipNextChars = true;
            } else {
                if (!skipNextChars) {
                    result.append(character);
                }
            }
        }

        return safetyCheckDomain(result.toString());
    }

    private String safetyCheckDomain(final String domain) {
        if (domain.isBlank()) {
            return null;
        }

        return domain;
    }
}
