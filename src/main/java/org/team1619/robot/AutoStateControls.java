package org.team1619.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 *  * This class allows for injection of a robot specific autoStateControls into the stateService
 */

public abstract class AutoStateControls extends StateControls {

	private static final Logger sLogger = LogManager.getLogger(AutoStateControls.class);

	public AutoStateControls(InputValues inputValues, RobotConfiguration robotConfiguration) {
		super(inputValues,robotConfiguration);
	}
}