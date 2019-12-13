package org.team1619.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.models.state.State;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 * The base for all state control logic
 */

public abstract class StateControls {
	private static final Logger sLogger = LogManager.getLogger(StateControls.class);

	protected final InputValues fSharedInputValues;
	protected final RobotConfiguration fRobotConfiguration;


	public StateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;
	}

	/**
	 * Called when switching into Teleop or Auto
	 * This is a place to zero subsystems, clear variables...
	 */
	public abstract void initialize();

	/**
	 * Called every frame
	 * This is a place read buttons, check modes...
	 */
	public abstract void update();

	/**
	 * Defines states that are not interruptable (need to be done before they can be disposed)
	 * @param name the name of the state being checked if is not interruptable
	 * @return whether the state is in the list of doNotInterrupt states
	 */
	public boolean doNotInterrupt(String name) {
		throw new ConfigurationException("doNotInterrupt " + name + " not overridden");
	}

	/**
	 * Defines states that are 'default' state
	 * @param name the name of the state being check if is a default state
	 * @return whether the state is in the list of default states
	 */
	public boolean isDefaultState(String name) {
		throw new ConfigurationException("isDefaultState " + name + " not overridden");
	}

	/**
	 * Defines when each state is ready
	 * @param name the name of the state being checked if it's ready
	 * @return whether the state is ready
	 */
	public boolean isReady(String name) {
		throw new ConfigurationException("isReady " + name + " not overridden");
	}

	/**
	 * Determines when a state is done
	 * This is a place to override the isDone logic in the behavior for a specific state
	 * @param name the state being checked if it's done
	 * @param state the state object (used to call the behavior's isDone)
	 * @return Whether the state is done
	 */
	public boolean isDone(String name, State state) {
		throw new ConfigurationException("isDone " + name + " not overridden");
	}

	/**
	 * Determines if a state is requesting to be disposed
	 * This is a place to override the isRequestingDisposal logic in the behavior for a specific state
	 * @param name the state being checked if it wants to be disposed
	 * @param state the state object (used to call the behavior's isRequestingDisposal)
	 * @return Whether the state wants to be disposed
	 */
	public boolean isRequestingDisposal(String name, State state) {
		throw new ConfigurationException("isRequestingDisposal " + name + " not overridden");
	}

	/**
	 * Called when switching between Auto, Teleop and Disabled
	 * This is a place for any clean up, clearing variables...
	 */
	public abstract void dispose();
}