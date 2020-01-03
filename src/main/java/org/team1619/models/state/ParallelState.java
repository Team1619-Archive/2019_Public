package org.team1619.models.state;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.robot.ModelFactory;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;
import java.util.*;

/**
 * A shell that handles running multiple states simultaneously
 * Assembles a list of all the states it wants to run and passes it to the state machine
 */

public class ParallelState implements State {

	private static final Logger sLogger = LogManager.getLogger(ParallelState.class);

	private final ModelFactory fModelFactory;
	private final String fStateName;

	private Set<State> fForegroundStates = new HashSet<>();
	private Set<State> fBackgroundStates = new HashSet<>();

	public ParallelState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
		fModelFactory = modelFactory;
		fStateName = name;

		for (Object foregroundStateName : config.getList("foreground_states")) {
			fForegroundStates.add(fModelFactory.createState((String) foregroundStateName, parser, parser.getConfig(foregroundStateName)));
		}

		for (Object backgroundStateName : config.getList("background_states")) {
			fBackgroundStates.add(fModelFactory.createState((String) backgroundStateName, parser, parser.getConfig(backgroundStateName)));
		}
	}

	@Override
	public List<State> getSubStates(){
		// Returns a list of all the state it is currently running
		List<State> states = new ArrayList<>();
		states.addAll(fForegroundStates);
		states.addAll(fBackgroundStates);
		return	states;
	}

	@Override
	public void initialize() {
		sLogger.info("Entering Parallel State {}", fStateName);
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving Parallel State {}", fStateName);
	}

	@Override
	public boolean isDone() {
		for (State foregroundState : fForegroundStates) {
			if (!foregroundState.isDone()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean isRequestingDisposal() {
		return isDone();
	}

	@Override
	public Set<String> getSubsystems() {
		// Returns a list of all the subsystems required by all the states it is running
		List<String> subsystems = new ArrayList<>();
		for(State foregroundState : fForegroundStates){
			subsystems.addAll(foregroundState.getSubsystems());
		}
		for(State backgroundState: fBackgroundStates){
			subsystems.addAll(backgroundState.getSubsystems());
		}
		return Set.copyOf(subsystems);
	}

	@Override
	public String getName() {
		return fStateName;
	}
}
