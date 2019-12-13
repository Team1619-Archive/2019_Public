package org.team1619.models.outputs.solenoids.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.outputs.solenoids.SolenoidDouble;
import org.team1619.utilities.Config;

public class SimSolenoidDouble extends SolenoidDouble {
	private static final Logger sLogger = LogManager.getLogger(SimSolenoidDouble.class);
	private boolean fOutput = false;

	public SimSolenoidDouble(Object name, Config config) {
		super(name, config);
	}

	@Override
	public void setHardware(boolean output) {
//        fOutput = output;
//        sLogger.trace("{}", output);
	}
}
