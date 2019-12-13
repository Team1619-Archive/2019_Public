package org.team1619.models.outputs.solenoids.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.outputs.solenoids.SolenoidSingle;
import org.team1619.utilities.Config;

public class SimSolenoidSingle extends SolenoidSingle {
	private static final Logger sLogger = LogManager.getLogger(SimSolenoidSingle.class);
	private boolean fOutput = false;

	public SimSolenoidSingle(Object name, Config config) {
		super(name, config);
	}

	@Override
	public void setHardware(boolean output) {
//        fOutput = output;
//        sLogger.trace("{}", output);
	}
}
