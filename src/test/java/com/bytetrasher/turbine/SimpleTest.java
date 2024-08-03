package com.bytetrasher.turbine;

import com.bytethrasher.turbine.Turbine;
import com.bytethrasher.turbine.location.container.DefaultLocationContainer;
import com.bytethrasher.turbine.process.starter.FixedSizeProcessStarter;
import org.junit.jupiter.api.Test;

public class SimpleTest {

    @Test
    public void simpleTest() {
        final Turbine turbine = Turbine.builder()
                .locationContainer(
                        DefaultLocationContainer.builder()
                                .maximumLocationsUnderProcessing(1)
                                .build()
                )
                .processStarter(
                        FixedSizeProcessStarter.builder()
                                .threadLimit(2)
                                .build()
                )
                .build();

        turbine.start();
    }
}
