package com.bytetrasher.turbine;

import com.bytethrasher.turbine.Turbine;
import org.junit.jupiter.api.Test;

public class SimpleTest {

    @Test
    public void simpleTest() {
        final Turbine turbine = Turbine.builder()
                .build();

        turbine.start();
    }
}
