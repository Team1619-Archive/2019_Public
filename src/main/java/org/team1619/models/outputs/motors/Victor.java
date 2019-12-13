package org.team1619.models.outputs.motors;

import org.team1619.utilities.Config;

/**
 * Victor is a motor object, which is extended to control victors
 */

public abstract class Victor extends CTREMotor {

	public Victor(Object name, Config config) {
		super(config.getInt("device_number"), name, config.getBoolean("brake_mode_enabled", true), config.getBoolean("inverted", false));
	}
}
