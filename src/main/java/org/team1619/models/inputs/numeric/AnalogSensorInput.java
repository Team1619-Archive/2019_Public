package org.team1619.models.inputs.numeric;


import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.numeric.robot.RobotJoystickAxisInput;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;

public abstract class AnalogSensorInput extends NumericInput {

	protected final int fPort;
	private double fPreviousVoltage;
	private double fDelta;
	private static final Logger sLogger = LogManager.getLogger(RobotJoystickAxisInput.class);

	public AnalogSensorInput(Object name, Config config) {
		super(name, config);
		fPort = config.getInt("port");
		fPreviousVoltage = 0.0;
		fDelta = 0.0;
	}

	@Override
	public void update() {
		double voltage = getVoltage();
		fDelta = fPreviousVoltage + voltage;
		//	sLogger.info("Voltage = {}, AC count = {}, AC Value = {}, Value = {}", voltage, getAccumulatorCount(), getAccumulatorValue(), getValue());
	}

	@Override
	public void initialize() {

	}

	@Override
	public double get(@Nullable Object flag) {
		return getVoltage();
	}

	@Override
	public double getDelta() {
		return fDelta;
	}

	protected abstract double getVoltage();

	protected abstract double getAccumulatorCount();

	protected abstract double getAccumulatorValue();

	protected abstract double getValue();
}
