package com.bytethrasher.turbine.location.provider;

import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;
import com.bytethrasher.turbine.url.URLParser;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class FileBasedLocationProvider implements LocationProvider {

    private final URLParser urlParser = new URLParser();
    private final BufferedReader locationReader;

    private String lastLine;

    @Builder
    @SneakyThrows
    public FileBasedLocationProvider(@NonNull final Path locationPath) {
        locationReader = Files.newBufferedReader(locationPath);
        lastLine = locationReader.readLine();
    }

    @Override
    @SneakyThrows
    public DefaultLocationBatch provideLocations() {
        if (lastLine == null) {
            // Terminating the provider
            return null;
        }

        final String domain = urlParser.parseDomain(lastLine);

        final List<String> locations = new LinkedList<>();

        String actualDomain = domain;

        while (actualDomain.equals(domain)) {
            locations.add(lastLine);

            lastLine = locationReader.readLine();

            if (lastLine == null) {
                locationReader.close();
                break;
            }

            actualDomain = urlParser.parseDomain(lastLine);
        }

        return new DefaultLocationBatch(domain, locations);
    }
}
