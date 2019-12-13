package org.team1619.models.inputs.vector.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.vector.AccelerometerInput;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.utilities.Config;

import java.util.HashMap;
import java.util.Map;

public class RobotAccelerationInput extends AccelerometerInput {

	private static final Logger sLogger = LogManager.getLogger(RobotNavxInput.class);

	private Map<String, Double> fNavxValues;
	private final InputValues fSharedInputValues;

	public RobotAccelerationInput(Object name, Config config, InputValues inputValues) {
		super(name, config);
		fSharedInputValues = inputValues;
		fNavxValues = new HashMap<>();
	}

	@Override
	public Map<String, Double> getAcceleration() {
		fNavxValues = fSharedInputValues.getVector("vi_navx", null);

		double xAcceleration = fNavxValues.getOrDefault("accel_x", 0.0);
		double yAcceleration = fNavxValues.getOrDefault("accel_y", 0.0);
		double zAcceleration = fNavxValues.getOrDefault("accel_z", 0.0);

		return Map.of("xAcceleration", xAcceleration, "yAcceleration", yAcceleration, "zAcceleration", zAcceleration);

	}
}
