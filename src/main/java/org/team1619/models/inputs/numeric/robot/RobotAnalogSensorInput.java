package org.team1619.models.inputs.numeric.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.numeric.AnalogSensorInput;
import org.team1619.utilities.Config;

public class RobotAnalogSensorInput extends AnalogSensorInput {

	private static final Logger sLogger = LogManager.getLogger(RobotJoystickAxisInput.class);

	private edu.wpi.first.wpilibj.AnalogInput fAnalogLogInput;

	public RobotAnalogSensorInput(Object name, Config config) {
		super(name, config);
		fAnalogLogInput = new edu.wpi.first.wpilibj.AnalogInput(fPort);
		fAnalogLogInput.resetAccumulator();

	}

	@Override
	public double getVoltage() {
		return fAnalogLogInput.getVoltage();
	}

	public double getAccumulatorCount() {
		return fAnalogLogInput.getAccumulatorCount();
	}

	public double getAccumulatorValue() {
		return fAnalogLogInput.getAccumulatorValue();
	}

	public double getValue() {
		return fAnalogLogInput.getValue();
	}

}
