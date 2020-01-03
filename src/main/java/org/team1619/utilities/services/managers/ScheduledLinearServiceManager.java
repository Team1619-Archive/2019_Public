package org.team1619.utilities.services.managers;

import org.team1619.utilities.services.Service;
import org.team1619.utilities.services.ServiceState;
import org.team1619.utilities.services.Scheduler;

import java.util.List;

/**
 * Runs the services sequentially using a scheduler
 */

public class ScheduledLinearServiceManager extends LinearServiceManager {

    private final Scheduler fScheduler;

    public ScheduledLinearServiceManager(Scheduler scheduler, Service... services) {
        this(scheduler, List.of(services));
    }

    public ScheduledLinearServiceManager(Scheduler scheduler, List<Service> services) {
        super(services);

        fScheduler = scheduler;
    }

    // States the services
    @Override
    public void start() {
        getExecutor().submit(() -> {
            fScheduler.start();
            super.startUp();

            while (getCurrentState() == ServiceState.RUNNING) {
                try {
                    Thread.sleep(fScheduler.millisecondsUntilNextRun());
                } catch (InterruptedException e) {}
                fScheduler.run();
                super.runUpdate();
            }

            super.shutDown();
        });
    }
}
