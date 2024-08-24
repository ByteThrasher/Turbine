package com.bytethrasher.turbine.location.provider;

import com.bytethrasher.turbine.location.provider.domain.DefaultLocationBatch;
import com.bytethrasher.turbine.url.URLParser;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Slf4j
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
            // Terminating the provider.
            return null;
        }

        final List<String> locations = new LinkedList<>();

        String domain = urlParser.parseDomain(lastLine);
        String actualDomain = domain;

        // The actualDomain can be null if the host can't be parsed from the URL aka location.
        while (domain == null || actualDomain == null || actualDomain.equals(domain)) {
            // We don't want to handle locations without a host.
            if (actualDomain != null) {
                locations.add(lastLine);
            } else {
                log.info("Skipping url because there is no parsable domain: {}.", lastLine);
            }

            lastLine = locationReader.readLine();

            // If lastLine is null then we reached the end of the file.
            if (lastLine == null) {
                locationReader.close();
                break;
            }

            actualDomain = urlParser.parseDomain(lastLine);

            // This fixes a bug that only happens when the first line's url has no valid domain.
            if (domain == null && actualDomain != null) {
                domain = actualDomain;
            }
        }

        return new DefaultLocationBatch(domain, locations);
    }
}
