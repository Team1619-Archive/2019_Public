package org.team1619.models.outputs.motors;

import org.team1619.shared.abstractions.InputValues;
import org.team1619.utilities.Config;

/**
 * Talon is a motor object, which is extended to control talons
 */

public abstract class Talon extends CTREMotor {

	protected final InputValues fSharedInputValues;

	protected final String fFeedbackDevice;

	protected final String fPositionInputName;
	protected final String fVelocityInputName;

	protected final boolean fHasEncoder;
	protected final boolean fSensorInverted;
	protected final boolean fCurrentLimitEnabled;
	protected final double fCountsPerUnit;
	protected final int fContinuousCurrentLimitAmps;
	protected final int fPeakCurrentLimitAmps;
	protected final int fPeakCurrentDurationMilliseconds;

	public Talon(Object name, Config config, InputValues inputValues) {
		super(config.getInt("device_number"), name, config.getBoolean("brake_mode_enabled", true), config.getBoolean("inverted", false));

		fSharedInputValues = inputValues;

		fPositionInputName = name.toString().replaceFirst("mo_", "ni_") + "_position";
		fVelocityInputName = name.toString().replaceFirst("mo_", "ni_") + "_velocity";

		fFeedbackDevice = config.getString("feedback_device", "");

		fHasEncoder = !fFeedbackDevice.isEmpty();
		fSensorInverted = config.getBoolean("sensor_inverted", false);
		fCurrentLimitEnabled = config.getBoolean("current_limit_enabled", false);
		fCountsPerUnit = config.getDouble("counts_per_unit", 1);
		fContinuousCurrentLimitAmps = config.getInt("continuous_current_limit_amps", 0);
		fPeakCurrentLimitAmps = config.getInt("peak_current_limit_amps", 0);
		fPeakCurrentDurationMilliseconds = config.getInt("peak_current_duration_milliseconds", 0);
	}

	protected void readEncoderValues() {
		if (!fHasEncoder) {
			return;
		}

		double sensorPosition = getSensorPosition();
		fSharedInputValues.setNumeric(fPositionInputName, sensorPosition);

		double sensorVelocity = getSensorVelocity();
		fSharedInputValues.setNumeric(fVelocityInputName, sensorVelocity);
	}

	public abstract double getSensorPosition();

	public abstract double getSensorVelocity();

	public abstract void zeroSensor();
}
