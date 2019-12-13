package org.team1619.robot.protobot1.state;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.state.State;
import org.team1619.robot.TeleopStateControls;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 * Determines which states are run
 */

public class Pro1_TeleopStateControls extends TeleopStateControls {

	private static final Logger sLogger = LogManager.getLogger(Pro1_TeleopStateControls.class);

	@Inject
	public Pro1_TeleopStateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
		super(inputValues, robotConfiguration);
	}

	// These are states that should be allowed to complete once they have started.
	// Only include states that cannot get stuck and never complete
	@Override
	public boolean doNotInterrupt(String name) {
		return false;
	}

	// These states will always be active if no other states wants their subsystem
	@Override
	public boolean isDefaultState(String name) {
		switch (name) {
		}
		return false;
	}

	@Override
	public void initialize() {
		if (!fSharedInputValues.getBoolean("bi_has_been_zeroed")) {
			fSharedInputValues.setBoolean("bi_has_been_zeroed", true);
		}
	}

	@Override
	public void update() {

	}

	@Override
	public boolean isReady(String name) {
		switch (name) {
			case "st_drive_percent":
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean isDone(String name, State state) {
		return state.isDone();
	}

	@Override
	public boolean isRequestingDisposal(String name, State state) {

		// Only active states are called. state.isRequestingDisposal should return false for states using the interrupt system
		//      and true for states using the default system
		switch (name) {
			default:
				return state.isRequestingDisposal();
		}
	}


	public void dispose() {
	}


}