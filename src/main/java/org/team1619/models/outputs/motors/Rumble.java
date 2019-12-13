package org.team1619.models.outputs.motors;

import org.team1619.utilities.Config;

/**
 * Rumble is a motor object, which is extended to control the xbox controller rumble motors
 */

public abstract class Rumble implements Motor {

	protected final Object fName;
	protected final int fPort;
	protected final String fRumbleSide;

	public Rumble(Object name, Config config) {
		fName = name;
		fPort = config.getInt("port");
		fRumbleSide = config.getString("rumble_side", "none");
	}

	public OutputType getMotorType(String name) {
		return OutputType.RUMBLE;
	}

	public int getPort() {
		return fPort;
	}
}
