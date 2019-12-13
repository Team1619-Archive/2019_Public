package org.team1619.models.state;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.robot.ModelFactory;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.utilities.Config;

import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The base for all states
 * One implementation is created for each state listed under 'single_state' in the state ymal file
 * Creates one copy of the associated behavior and points all states that use it to the same instance
 * Watches after the associated behavior
 */

public class SingleState implements State {

	private static final Logger sLogger = LogManager.getLogger(SingleState.class);

	private final ModelFactory fModelFactory;
	private final String fStateName;
	private final ObjectsDirectory fSharedObectsDirectory;

	private Behavior fBehavior;

	private String fBehaviorName;
	private Config fBehaviorConfig;

	public SingleState(ModelFactory modelFactory, String name, Config config, ObjectsDirectory objectsDirectory) {
		fModelFactory = modelFactory;
		fStateName = name;
		fSharedObectsDirectory = objectsDirectory;

		fBehaviorName = config.getString("behavior");
		fBehaviorConfig = config.getSubConfig("behavior_config", "behavior_config");

		// Only create a new behavior class instance if it has not already been created by another state
		// A single instance allows all states using this behvavior class to share member variable information inside the single instance
		// Behavior.Intialize() is called each time a new state is entered and Behavior.Dispose() is called when leaving the state
		fBehavior = fSharedObectsDirectory.getBehaviorObject(fBehaviorName);
		//noinspection ConstantConditions
		if (fBehavior == null) {
			fBehavior = fModelFactory.createBehavior(fBehaviorName, fBehaviorConfig);
			fSharedObectsDirectory.setBehaviorObject(fBehaviorName, fBehavior);
		}

	}

	@Override
	public List<State> getSubStates(){
	    return	Arrays.asList(this);
	}

	@Override
	public void initialize() {
		fBehavior.initialize(fStateName, fBehaviorConfig);
	}

	@Override
	public void update() {
		checkNotNull(fBehavior);
		fBehavior.update();

	}

	@Override
	public void dispose() {
		checkNotNull(fBehavior);
		fBehavior.dispose();
	}

	@Override
	public boolean isDone() {
		checkNotNull(fBehavior);
		return fBehavior.isDone();
	}

	@Override
	public boolean isRequestingDisposal() {
		checkNotNull(fBehavior);
		return fBehavior.isRequestingDisposal();
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		return fBehavior.getSubsystems();
	}

	@Override
	public String getName() {
		return fStateName;
	}
}
