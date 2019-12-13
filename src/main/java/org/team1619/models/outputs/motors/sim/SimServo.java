package org.team1619.models.outputs.motors.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * SimServo extends Servo, and acts like servo motors in sim mode
 */

public class SimServo extends org.team1619.models.outputs.motors.Servo {
	private static final Logger sLogger = LogManager.getLogger(SimServo.class);

	private double fOutput = 0.0;

	public SimServo(Object name, Config config) {
		super(name, config);
	}

	@Override
	public void setHardware(Motor.OutputType outputType, double outputValue, @Nullable Object flag) {
		fOutput = outputValue;
//		sLogger.trace("{}", outputValue);
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		motorCurrentValues.put(fChannel, -1.0);
		return motorCurrentValues;
	}
}
