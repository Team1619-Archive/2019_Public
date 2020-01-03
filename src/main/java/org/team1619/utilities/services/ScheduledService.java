package org.team1619.utilities.services;

/**
  *  Configures one service to run on a scheduler
 */

public abstract class ScheduledService extends Service {

    protected abstract Scheduler scheduler();
}
