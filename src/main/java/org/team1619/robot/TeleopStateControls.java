package org.team1619.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 * This class allows for injection of a robot specific teleopStateControls into the stateService
 */

public abstract class
TeleopStateControls extends StateControls {

	private static final Logger sLogger = LogManager.getLogger(TeleopStateControls.class);

	public TeleopStateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
		super(inputValues, robotConfiguration);
	}
}