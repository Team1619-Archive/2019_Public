package org.team1619.models.state;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.robot.ModelFactory;
import org.team1619.utilities.Config;
import org.team1619.utilities.Timer;
import org.team1619.utilities.YamlConfigParser;

import java.util.Arrays;
import java.util.List;

/**
 * A shell that runs a state until it is done or it times out
 * I'm not sure this is useful in the current version of the framework
 */

public class TimedState implements State {

	private static final Logger sLogger = LogManager.getLogger(TimedState.class);

	private final String fStateName;
	private final State fSubState;
	private final String fSubStateName;
	private final Timer fTimer = new Timer();
	private final int fTimeout;

	public TimedState(ModelFactory modelFactory, String name, YamlConfigParser parser, Config config) {
		fStateName = name;

		fSubStateName = config.getString("state");
		fSubState = modelFactory.createState(fSubStateName, parser, parser.getConfig(fStateName));
		fTimeout = config.getInt("timeout");
	}

	@Override
	public List<State> getSubStates(){
		return	Arrays.asList(fSubState);
	}

	@Override
	public void initialize() {
		sLogger.info("Entering Timed State {}", fStateName);
		fTimer.start(fTimeout);
	}

	@Override
	public void update() {
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving Timed State {}", fStateName);
		fTimer.reset();
	}

	@Override
	public boolean isDone() {
		return fSubState.isDone() || fTimer.isDone();
	}

	@Override
	public boolean isRequestingDisposal() {
		return isDone();
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		return fSubState.getSubsystems();
	}

	@Override
	public String getName() {
		return fStateName;
	}
}
