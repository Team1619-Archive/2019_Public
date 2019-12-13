package org.team1619.models.inputs.vector;

import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

// Ymal config file block
// navx:
//	 vi_navx:
//		yaw_is_radians: true
//		roll_is_radians: true
//		pitch_is_radians: true
//		angle_is_radians: true
//		compass_is_radians: true
//		fused_heading_is_radians: true
//		yaw_is_inverted: true
//		roll_is_inverted: true
//		pitch_is_inverted: true
//		angle_is_inverted: true
//		compass_is_inverted: true
//		fused_heading_is_inverted: true
//		accel_x_is_inverted: true
//		accel_y_is_inverted: true
//		accel_z_is_inverted: true

public abstract class NavxInput extends VectorInput {

	protected Map<String, Boolean> fIsRaidans;
	protected Map<String, Boolean> fIsInverted;
	protected Map<String, Double> fNavxValues = new HashMap<>();

	public NavxInput(Object name, Config config) {
		super(name, config);

		//Is Inverted
		fIsInverted = new HashMap<>();
		fIsInverted.put("yaw", config.getBoolean("yaw_is_inverted", false));
		fIsInverted.put("roll", config.getBoolean("roll_is_inverted", false));
		fIsInverted.put("pitch", config.getBoolean("pitch_is_inverted", false));
		fIsInverted.put("compass", config.getBoolean("compass_is_inverted", false));
		fIsInverted.put("angle", config.getBoolean("angle_is_inverted", false));
		fIsInverted.put("fused_heading", config.getBoolean("fused_heading_is_inverted", false));
		fIsInverted.put("accel_x", config.getBoolean("accel_x_is_inverted", false));
		fIsInverted.put("accel_y", config.getBoolean("accel_y_is_inverted", false));
		fIsInverted.put("accel_z", config.getBoolean("accel_z_is_inverted", false));

		// Is radians
		fIsRaidans = new HashMap<>();
		fIsRaidans.put("yaw", config.getBoolean("yaw_is_radians", false));
		fIsRaidans.put("roll", config.getBoolean("roll_is_radians", false));
		fIsRaidans.put("pitch", config.getBoolean("pitch_is_radians", false));
		fIsRaidans.put("compass", config.getBoolean("compass_is_radians", false));
		fIsRaidans.put("angle", config.getBoolean("angle_is_radians", false));
		fIsRaidans.put("fused_heading", config.getBoolean("fused_heading_is_radians", false));
	}

	@Override
	public void update() {
		fNavxValues = readHardware();
	}

	@Override
	public void initialize() {
		fNavxValues = Map.of("Yaw", 0.0, "roll", 0.0, "pitch", 0.0, "compass", 0.0, "angle", 0.0, "fused_heading", 0.0, "accel_x", 0.0, "accel_y", 0.0, "accel_z", 0.0);
	}

	@Override
	public Map<String, Double> get(@Nullable Object flag) {
		if (flag instanceof String && flag.equals("zero")) {
			zeroYaw();
		}
		return fNavxValues;
	}

	protected abstract Map<String, Double> readHardware();

	protected abstract void zeroYaw();


}
