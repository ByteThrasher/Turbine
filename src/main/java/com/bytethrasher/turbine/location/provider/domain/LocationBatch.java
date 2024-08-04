package com.bytethrasher.turbine.location.provider.domain;

import java.util.List;

public interface LocationBatch {

    String domain();

    List<String> locations();
}
