package org.team1619.utilities.services.managers;

import org.team1619.utilities.services.Service;
import org.team1619.utilities.services.ServiceState;
import org.team1619.utilities.services.ServiceWrapper;

import java.util.List;

/**
 *  Runs the services as fast as possible skipping over ones that have not completed their frames
 */

public class SynchronizedServiceManager extends ServiceManager {

    public SynchronizedServiceManager(Service... services) {
        this(List.of(services));
    }

    public SynchronizedServiceManager(List<Service> services) {
        super(services);

        setCurrentState(ServiceState.AWAITING_START);
    }

    @Override
    protected void onError(ServiceWrapper service, Exception exception) {
        RuntimeException e = new RuntimeException(service.getServiceName() + " has failed in a " + service.getServiceState() + " state", exception);

        e.setStackTrace(new StackTraceElement[]{});

        e.printStackTrace();

        if(getCurrentState() == ServiceState.STARTING) {
            stop();
        }
    }

    // States the services
    @Override
    public void start() {
        getExecutor().submit(() -> {
            setCurrentState(ServiceState.STARTING);

            for (ServiceWrapper service : getServices()) {
                startUpService(service);
            }

            if(getCurrentState() != ServiceState.STOPPING) {
                setCurrentState(ServiceState.RUNNING);

                while (getCurrentState() == ServiceState.RUNNING) {
                    update();
                }
            }

            for (ServiceWrapper service : getServices()) {
                shutDownService(service);
            }

            setCurrentState(ServiceState.STOPPED);
        });
    }

    @Override
    public void awaitHealthy() {
        while (getCurrentState().equals(ServiceState.AWAITING_START) || getCurrentState().equals(ServiceState.STARTING));
    }

    @Override
    public void update() {
        for (ServiceWrapper service : getServices()) {
            if (service.shouldRun()) {
                service.setCurrentlyRunning(true);
                updateService(service);
            }
        }
    }

    @Override
    public void stop() {
        setCurrentState(ServiceState.STOPPING);
    }

    @Override
    public void awaitStopped() {
        while (!getCurrentState().equals(ServiceState.STOPPED));
    }
}
