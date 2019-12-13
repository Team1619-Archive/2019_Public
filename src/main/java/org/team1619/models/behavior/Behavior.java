package org.team1619.models.behavior;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.Config;

public interface Behavior {

	/**
	 * @return a list of subsystems required by the behavior
	 */
	ImmutableSet<String> getSubsystems();

	/**
	 * Initializes the behavior to run the requested state
	 * This is a place to read values from the state config file
	 * This is called by the StateMachine on the first frame a state becomes active
	 * @param stateName The name of the state being initialized
	 * @param config Contains the information under the name of the state in the state.ymal file
	 */
	void initialize(String stateName, Config config);

	/**
	 * Called every frame when a state is active
	 * This is a place to read buttons, do calculations, set motor outputs...
	 */
	void update();

	/**
	 * This holds the logic that determines when a state has finished its task
	 * In state mode this logic is largely ignored unless the state requests disposal (see idRequestingDisposal) when it is finished
	 * In Sequence mode this logic lets the sequencer that is running this state know when to move onto the next state
	 * This logic can be overridden in implementations of StateControls for specific states
	 * @return true or false
	 */
	boolean isDone();

	/**
	 * Tells the StateMachine when to dispose the state
	 * If the state is running under the interrupt system (stays in the current state until interrupted.) this should be set to false
	 * If the state is running with the idle/default state system (Runs a state until it finishes and then drops into a separate default/idle state.)
	 *      this should be set to isDone.
	 * This logic can be overridden in implementations of StateControls for specific states
	 * @return true or false
	 */
	boolean isRequestingDisposal();

	/**
	 * Called when the state becomes inactive
	 * This is a place to set motors to 0.0, clear variables, post positions... Anything you want to happen when the state finishes.
	 */
	void dispose();
}
