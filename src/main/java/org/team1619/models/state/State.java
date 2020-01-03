package org.team1619.models.state;

import java.util.List;
import java.util.Set;

public interface State {

	/**
	 * @return the name of the state
	 */
	String getName();

	/**
	 * @return a list of subsystems required by the state
	 */
	Set<String> getSubsystems();

	/**
	 * @return a list of states managed by this state
	 * A SingleState returns itself
	 * SequencerState, ParallelState, DoneForTimeState, and TimedState return the list of states they are managing in the current frame
	 */
	List<State> getSubStates();

	/**
	 * Called when a state becomes active
	 */
	void initialize();

	/**
	 * Called every frame when a state is active
	 */
	void update();

	/**
	 * @return whether the state has completed its task
	 * This can be determined by the behavior's isDone, when a timer runs out, the sequence is finished ...
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
	 */
	void dispose();
}
