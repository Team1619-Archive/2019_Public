package org.team1619.utilities.services;

import javax.annotation.Nullable;

/**
  *  Manages one service with or without a scheduler
 */

public class ServiceWrapper extends Service {

    private final Service fService;

    @Nullable
    private final Scheduler scheduler;

    private boolean fIsCurrentlyRunning = false;

    private ServiceState fServiceState = ServiceState.AWAITING_START;

    public ServiceWrapper(Service service) {
        fService = service;

        // Gets a scheduler if it is a scheduled service
        if (fService instanceof ScheduledService) {
            scheduler = ((ScheduledService) fService).scheduler();
        } else {
            scheduler = null;
        }
    }

    // Returns the current state of the service
    public ServiceState getServiceState() {
        synchronized (fServiceState) {
            return fServiceState;
        }
    }

    // Determines whether a service should run based on the service's current state and a scheduler if included in the service
    public boolean shouldRun() {
        synchronized (fServiceState) {
            if (fServiceState == ServiceState.AWAITING_START || fServiceState == ServiceState.STOPPING) {
                return false;
            }
        }

        // Do nothing if runOneIteration in the service is currently running
        if (isCurrentlyRunning()) {
            return false;
        }

        if (scheduler != null) {
            return scheduler.shouldRun();
        }

        return true;
    }

    public boolean isCurrentlyRunning() {
        return fIsCurrentlyRunning;
    }

    public void setCurrentlyRunning(boolean currentlyRunning) {
        fIsCurrentlyRunning = currentlyRunning;
    }

    public String getServiceName() {
        return fService.getClass().getSimpleName();
    }

    //Starts the scheduler and starts the service
    @Override
    public synchronized void startUp() throws Exception {
        Thread.currentThread().setName(getServiceName());
        synchronized (fServiceState) {
            fServiceState = ServiceState.STARTING;
        }
        if (scheduler != null) {
            scheduler.start();
        }
        fService.startUp();
    }

    // Calls runOneIteration on the service and waits for it to complete before moving on
    @Override
    public synchronized void runOneIteration() throws Exception {
        Thread.currentThread().setName(getServiceName());
        fIsCurrentlyRunning = true;
        if (scheduler != null) {
            scheduler.run();
        }
        synchronized (fServiceState) {
            fServiceState = ServiceState.RUNNING;
        }
        try {
            fService.runOneIteration();
        } catch (Exception e) {
            fIsCurrentlyRunning = false;
            throw e;
        }
        fIsCurrentlyRunning = false;
    }

    // Shuts down the service
    @Override
    public synchronized void shutDown() throws Exception {
        Thread.currentThread().setName(getServiceName());
        synchronized (fServiceState) {
            fServiceState = ServiceState.STOPPING;
        }
        fService.shutDown();
        synchronized (fServiceState) {
            fServiceState = ServiceState.STOPPED;
        }
    }
}
