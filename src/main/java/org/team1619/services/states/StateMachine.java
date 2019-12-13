package org.team1619.services.states;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.state.SingleState;
import org.team1619.models.state.State;
import org.team1619.robot.StateControls;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.shared.abstractions.RobotConfiguration;

import java.util.*;

/**
 * Determines the life cycles of states and the priority of states who want to become active
 */


public class StateMachine {

	private static final Logger sLogger = LogManager.getLogger(StateMachine.class);

	private final ObjectsDirectory fSharedObjectsDirectory;
	private StateControls fStateControls;
	private final RobotConfiguration fRobotConfiguration;
	private final InputValues fSharedInputValues;

	private Set<State> fPrimaryActiveStates = new HashSet<>();
	private Set<State> fActiveStates = new HashSet<>();

	public StateMachine(ObjectsDirectory objectsDirectory, StateControls stateControls, RobotConfiguration robotConfiguration, InputValues inputValues) {
		fSharedObjectsDirectory = objectsDirectory;
		fStateControls = stateControls;
		fRobotConfiguration = robotConfiguration;
		fSharedInputValues = inputValues;
	}

	/**
	 * Allows the instance of StateControls to be changed out when switching from Auto to Teleop
	 * @param stateControls and instance of StateControls
	 */
	public void initialize(StateControls stateControls){
		fStateControls = stateControls;
	}

	/**
	 * Called every frame by the state service
	 */
	public void update() {

		// Get a list of states that will be active in this frame
		Set<State> nextActiveStates = getNextActiveStates();

		// Dispose of the states that became inactive in this frame
		disposeInactiveStates(nextActiveStates);

		// Initialize states that became active in this frame
		initializeNewlyActiveStates(nextActiveStates);

		fActiveStates = nextActiveStates;

		// Update all active states
		for (State state : fActiveStates) {
			state.update();
		}
	}

	/**
	 * Called when switching into disable mode
	 * Clears lists of currently active state and calls update to cause them to be disposed
	 */
	public void dispose() {

		// Dispose all active states
		for (State state : fActiveStates) {
			state.dispose();
		}

		fActiveStates.clear();
		fPrimaryActiveStates.clear();

		update();
	}

	/**
	 * @return a list of states that will be active in this frame
	 */
	private Set<State> getNextActiveStates() {
		Set<State> nextActiveStates = new HashSet<>();
		Set<State> primaryNextActiveStates = new HashSet<>();
		Set<State> nonActiveStatesThatAreReady = new HashSet<>();
		Set<State> currentlyActiveStates = new HashSet<>();
		Set<State> defaultStates = new HashSet<>();
		Set<String> subsystems = fRobotConfiguration.getSubsystemNames();
		Set<State> currentlyActuveStatesThatAreNotInterruptible = new HashSet<>();

		// Loop through every possible state
		for (String name : fRobotConfiguration.getStateNames()) {

			// Return now if there are no available subsystems
			if (subsystems.isEmpty()) {
				break;
			}

			State state = fSharedObjectsDirectory.getStateObject(name);

			// Is this state ready?
			boolean isReady = fStateControls.isReady(name);

			// Can this state be interrupted?
			boolean doNotInterrupt = fStateControls.doNotInterrupt(name);

			// Is this state a default state
			boolean isDefaultSate = fStateControls.isDefaultState(name);

			// Is this state currently active?
			boolean isCurrentlyActive = fPrimaryActiveStates.contains(state);

			// If the state is currently active, then check if it is done
			boolean isDone = true;
			if (fPrimaryActiveStates.contains(state)) {
				isDone = fStateControls.isDone(name, state);
			}

			// If the state is currently active, then check if it is requesting disposal
			boolean isRequestingDisposal = false;
			if (fPrimaryActiveStates.contains(state)) {
				isRequestingDisposal = fStateControls.isRequestingDisposal(name, state);
			}

			// Add primary states based on four priorities
			// Primary states are the states declared in StateControls - Non-primary states are states managed by sequence, parallel and timed states
			// First add currently active states that are not done yet and cannot be interrupted. This allows them to complete, but will hang up the system if they do not complete. So include a timer to ensure the state will always finish.
			// Next add non-active states that are ready. This allows a state to interrupt a currently active state that is of this same priority level.
			// Next add currently active states. This allows states that are current running to continue running as long as no other state wants to interrupt.
			// Next add any default states
			// Each state takes the subsystem(s) it requires in order of the priority above. Once the subsystem is taken, all lower priority states have to wait until the subsystem(s) it requires becomes available.
			if (isCurrentlyActive && doNotInterrupt && !isDone) {
				// If the state is currently active and can not be interrupted, then add it to a list that will be given first priority
				currentlyActuveStatesThatAreNotInterruptible.add(state);
			} else if (!isCurrentlyActive && isReady && !isDefaultSate) {
				// If the state is not currently active, but is ready and not a default state, then add it to a list that will be given second priority
				nonActiveStatesThatAreReady.add(state);
			} else if (isCurrentlyActive && !isRequestingDisposal) {
				// If the state is currently active and not requesting disposal, then add to a list that will be given third priority
				currentlyActiveStates.add(state);
			}else if (isReady) {
				// If the state is ready (at this point, this only applies to default states), then add to a list to give fourth priority
				defaultStates.add(state);
			}
		}

		// Add any states that are currently active and are non-interruptible to the list of next active states
		for (State state : currentlyActuveStatesThatAreNotInterruptible) {
			if (isSubsystemAvailable(state, subsystems)) {
				primaryNextActiveStates.add(state);
			}
		}

		// Add any states that are not currently active and are ready whose subsystem(s) are still available to the list of primary next active states
		for (State state : nonActiveStatesThatAreReady) {
			if (isSubsystemAvailable(state, subsystems)) {
				primaryNextActiveStates.add(state);
			}
		}

		// Add any states that are currently active whose subsystem(s) are still available to the list of primary next active states
		for (State state : currentlyActiveStates) {
			if (isSubsystemAvailable(state, subsystems)) {
				primaryNextActiveStates.add(state);
			}
		}

		// Add any default states whose subsystem(s) are still available to the list of primary next active states
		for (State state : defaultStates) {
			if (isSubsystemAvailable(state, subsystems)) {
				primaryNextActiveStates.add(state);
			}
		}

		// Add all primaryNextActiveStates to the list of nextActiveStates
		nextActiveStates.addAll(primaryNextActiveStates);

		// Create a new list of subsystems to determine which subsystems are still available since the sequencer state only runs one state at a time
		subsystems = fRobotConfiguration.getSubsystemNames();

		// Add all states managed by the primary active states to the list of nextActiveStates
		// And remove the subsystems for these substates to determine which subsystems are still available
		for(State primaryState : primaryNextActiveStates){
			for(State subState : primaryState.getSubStates()){
				if(!nextActiveStates.contains(subState)) {
					nextActiveStates.add(subState);
				}
				if(subState instanceof SingleState){
					isSubsystemAvailable(subState, subsystems);
				}
			}
		}

		// Add any states that are currently active whose subsystem(s) are still available
		for (State state : fActiveStates){
			if(state instanceof SingleState) {
				if (isSubsystemAvailable(state, subsystems)) {
					nextActiveStates.add(state);
				}
			}
		}

		// Store the currently active primary states for use in the next frame.
		fPrimaryActiveStates = primaryNextActiveStates;

		// Store a list of currently active states in the SharedInputValues so it can be displayed on the web dashboard for debugging
		String activeStatesList = "";
		for (State state : nextActiveStates) {
			activeStatesList += ((state.getName().toString()) + ", ");
		}
		fSharedInputValues.setString("active states", activeStatesList);


		return nextActiveStates;
	}

	/**
	 * Tracks the available subsystems
	 * If the subsystem requested by the state is available, removes the subsystem from the list of available subsystems
	 * @param state the state that is requesting a subsystem
	 * @param subsystems the list of subsystems to check
	 * @return whether the requested subsystem is available
	 */
	private boolean isSubsystemAvailable(State state, Set<String> subsystems) {

		// Check if the required subsystems for this state is available
		boolean valid = true;
		for (String subsystemName : state.getSubsystems()) {
			if (!subsystems.contains(subsystemName)) {
				valid = false;
				break;
			}
		}

		// If the subsystems are available then remove them from the list so they can not be used again
		if(valid) {
			for (String subsystemName : state.getSubsystems()) {
				subsystems.remove(subsystemName);
			}
		}

		return valid;
	}

	/**
	 * Loops through all states listed in robotconfigruation.ymal and calls initialize on any states that are becoming active this frame
	 * @param nextActiveStates the list of states that are active this frame
	 */
	private void initializeNewlyActiveStates(Set<State> nextActiveStates) {
		for (String name : fRobotConfiguration.getStateNames()) {
			State state = fSharedObjectsDirectory.getStateObject(name);

			boolean isInCurrent = fActiveStates.contains(state);
			boolean isInNext = nextActiveStates.contains(state);

			if (isInNext && !isInCurrent) {
				state.initialize();

			}
		}
	}

	/**
	 * Loops through all states listed in robotconfigruation.ymal and calls dispose on any states that are becoming inactive this frame
	 * @param nextActiveStates the list of states that are active this frame
	 */
	private void disposeInactiveStates(Set<State> nextActiveStates) {
		for (String name : fRobotConfiguration.getStateNames()) {
			State state = fSharedObjectsDirectory.getStateObject(name);

			boolean isInCurrent = fActiveStates.contains(state);
			boolean isInNext = nextActiveStates.contains(state);

			if (isInCurrent && !isInNext) {
				state.dispose();
			}
		}
	}


	/**
	 * @return the list of states that are active this frame
	 */
	public Set<State> getCurrentActiveStates() {
		return fActiveStates;
	}
}
