package org.team1619.utilities.services;

/**
 *  Configures one service
 */

public abstract class Service {

    protected abstract void startUp() throws Exception;

    protected abstract void runOneIteration() throws Exception;

    protected abstract void shutDown() throws Exception;
}
