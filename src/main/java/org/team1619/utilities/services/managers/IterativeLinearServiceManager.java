package org.team1619.utilities.services.managers;

import org.team1619.utilities.services.Service;
import org.team1619.utilities.services.ServiceState;

import java.util.List;

/**
  *  Runs the services sequentially as fast a possible
 */

public class IterativeLinearServiceManager extends LinearServiceManager {

    public IterativeLinearServiceManager(Service... services) {
        this(List.of(services));
    }

    public IterativeLinearServiceManager(List<Service> services) {
        super(services);
    }

    @Override
    public void start() {
        getExecutor().submit(() -> {
            super.startUp();

            while (getCurrentState() == ServiceState.RUNNING) {
                runUpdate();
            }

            super.shutDown();
        });
    }
}
