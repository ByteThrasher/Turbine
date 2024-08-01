package com.bytethrasher.turbine.process.starter;

import com.bytethrasher.turbine.location.container.LocationContainer;

public interface ProcessStarter {

    boolean atProcessLimit();

    void waitUntilFinish();

    void startProcess(String domain, LocationContainer locationContainer);
}
