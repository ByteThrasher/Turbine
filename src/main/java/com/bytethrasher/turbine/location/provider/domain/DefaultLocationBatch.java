package com.bytethrasher.turbine.location.provider.domain;

import java.util.List;

public record DefaultLocationBatch(

        String domain,
        List<String> locations
) implements LocationBatch {
}
