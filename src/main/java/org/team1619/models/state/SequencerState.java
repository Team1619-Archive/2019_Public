package org.team1619.models.state;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.robot.ModelFactory;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

import java.util.ArrayList;
import java.util.List;

/**
 * A shell that handles running a sequence of states
 * Passes the current state it wants to run to the state machine
 */

public class SequencerState implements State {

	private static final Logger sLogger = LogManager.getLogger(SequencerState.class);

	private final ModelFactory fModelFactory;
	private final String fStateName;

	private List<State> fStates = new ArrayList<>();
	private int fCurrentStateIndex;
	private State fCurrentState;

	public SequencerState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
		fModelFactory = modelFactory;
		fStateName = name;

		for (Object stateName : config.getList("sequence")) {
			fStates.add(fModelFactory.createState((String) stateName, parser, parser.getConfig(stateName)));
		}

		fCurrentStateIndex = 0;
		fCurrentState = fStates.get(fCurrentStateIndex);
	}

	@Override
	public List<State> getSubStates(){
		// returns a list of all the states it is currently running in this frame
		List<State> states = new ArrayList<>();
		states.add(fCurrentState);
		for(State subState : fCurrentState.getSubStates()){
			if(!states.contains(subState)){
				states.add(subState);
			}
		}
		return states;
	}

	@Override
	public void initialize() {
		sLogger.info("");
		sLogger.info("Entering Sequencer State {}", fStateName);
	}

	@Override
	public void update() {

		if (fCurrentStateIndex >= fStates.size()) {
			return;
		}
		// Increments through the sequence
		if (fCurrentState.isDone()) {
			if(fCurrentStateIndex < (fStates.size())) {
				fCurrentStateIndex++;
				if(fCurrentStateIndex < (fStates.size())) {
					fCurrentState = fStates.get(fCurrentStateIndex);
				}
			}
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving Sequencer State {}", fStateName);
		fCurrentStateIndex = 0;
		fCurrentState = fStates.get(fCurrentStateIndex);
	}

	@Override
	public boolean isDone() {
		return fCurrentStateIndex >= fStates.size();
	}

	@Override
	public boolean isRequestingDisposal() {
		return isDone();
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		// Returns a list of all the subsystems required by all the states that will be run sometime during the sequence
		List<String> subsystems = new ArrayList<>();
		for (State state : fStates) {
			for (String subsystem : state.getSubsystems()) {
				if (!subsystems.contains(subsystem)) {
					subsystems.add(subsystem);
				}
			}
		}
		return ImmutableSet.copyOf(subsystems);
	}

	@Override
	public String getName() {
		return fStateName;
	}

}
