package org.team1619.models.outputs.motors.sim;

import org.team1619.models.outputs.motors.Rumble;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * SimRumble extends Rumble, and acts like xbox controller rumble motors in sim mode
 */

public class SimRumble extends Rumble {

	private double fOutput;
	private String fRumbleSide;
	private double fAdjustedOutput;

	public SimRumble(Object name, Config config) {
		super(name, config);
		fOutput = 0.0;
		fAdjustedOutput = 0.0;
		fRumbleSide = "none";
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		fAdjustedOutput = outputValue;
		if (fRumbleSide.equals("right")) {
			fOutput = fAdjustedOutput;
			fRumbleSide = "right";
		} else if (fRumbleSide.equals("left")) {
			fOutput = fAdjustedOutput;
			fRumbleSide = "left";
		}
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		motorCurrentValues.put(fPort, fAdjustedOutput);
		return motorCurrentValues;
	}
}