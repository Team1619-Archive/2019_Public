package org.team1619.models.inputs.vector.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.vector.NavxInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.utilities.Config;

import java.util.HashMap;
import java.util.Map;

public class SimNavxInput extends NavxInput {

	private SimVectorInputListener fListener;
	private static final Logger sLogger = LogManager.getLogger(SimNavxInput.class);

	private double fNavxYaw;
	private double fNavxRoll;
	private double fNavxPitch;
	private double fNavxCompass;
	private double fNavxAngle;
	private double fNavxFusedHeading;
	private double fNavxAccelX;
	private double fNavxAccelY;
	private double fNavxAccelZ;


	public SimNavxInput(EventBus eventBus, Object name, Config config) {
		super(name, config);

		fNavxYaw = 0.0;
		fNavxRoll = 0.0;
		fNavxPitch = 0.0;
		fNavxCompass = 0.0;
		fNavxAngle = 0.0;
		fNavxFusedHeading = 0.0;
		fNavxAccelX = 0.0;
		fNavxAccelY = 0.0;
		fNavxAccelX = 0.0;

		fListener = new SimVectorInputListener(eventBus, name, Map.of("yaw", fNavxYaw, "roll", fNavxRoll, "pitch", fNavxPitch, "compass", fNavxCompass, "angle", fNavxAngle, "fused_heading", fNavxFusedHeading, "accel_x", fNavxAccelX, "accel_y", fNavxAccelY, "accel_z", fNavxAccelZ));
	}

	protected Map<String, Double> readHardware() {

		//Inverted
		fNavxYaw = getValue("yaw");
		fNavxRoll = getValue("roll");
		fNavxPitch = getValue("pitch");
		fNavxCompass = getValue("compass");
		fNavxAngle = getValue("angle");
		fNavxFusedHeading = getValue("fused_heading");
		fNavxAccelX = getValue("accel_x");
		fNavxAccelY = getValue("accel_y");
		fNavxAccelZ = getValue("accel_z");

		return Map.of("yaw", fNavxYaw, "roll", fNavxRoll, "pitch", fNavxPitch, "compass", fNavxCompass, "angle", fNavxAngle, "fused_heading", fNavxFusedHeading, "accel_x", fNavxAccelX, "accel_y", fNavxAccelY, "accel_z", fNavxAccelZ);
	}

	private double getValue(String name) {
		double value = fIsInverted.get(name) ? fListener.get().get(name) * -1 : fListener.get().get(name);
		return (fIsRaidans.containsKey(name) && fIsRaidans.get(name)) ? value * Math.PI / 180 : value;
	}

	protected void zeroYaw() {
		sLogger.info("SimNavxInput -> Zeroing yaw");
		double yaw = fNavxValues.get("yaw");

		Map<String, Double> lastNavxValues = fNavxValues;
		fNavxValues = new HashMap<>();
		fNavxValues.putAll(lastNavxValues);

		fNavxValues.put("yaw", 0.0);
	}
}
