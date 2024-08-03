package com.bytethrasher.turbine.location.provider.domain;

import java.util.List;

public record LocationBatch(

        String domain,
        List<String> locations
) {
}
