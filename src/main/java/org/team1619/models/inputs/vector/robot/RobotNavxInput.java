package org.team1619.models.inputs.vector.robot;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.SPI;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.vector.NavxInput;
import org.team1619.utilities.Config;

import java.util.Map;

public class RobotNavxInput extends NavxInput {

	private static final Logger sLogger = LogManager.getLogger(RobotNavxInput.class);

	private AHRS fNavx;
	private double fNavxYaw;
	private double fNavxRoll;
	private double fNavxPitch;
	private double fNavxCompass;
	private double fNavxAngle;
	private double fNavxFusedHeading;
	private double fNavxAccelX;
	private double fNavxAccelY;
	private double fNavxAccelZ;


	public RobotNavxInput(Object name, Config config) {
		super(name, config);

		fNavx = new AHRS(SPI.Port.kMXP);
		fNavx.zeroYaw();

		fNavxYaw = 0.0;
		fNavxRoll = 0.0;
		fNavxPitch = 0.0;
		fNavxCompass = 0.0;
		fNavxAngle = 0.0;
		fNavxFusedHeading = 0.0;
		fNavxAccelX = 0.0;
		fNavxAccelY = 0.0;
		fNavxAccelX = 0.0;
	}

	@Override
	protected void zeroYaw() {
		sLogger.info("RobotNavxInput -> Zeroing yaw");
		fNavx.zeroYaw();
	}

	@Override
	protected Map<String, Double> readHardware() {

		// Inverted
		fNavxYaw = fIsInverted.get("yaw") ? fNavx.getYaw() * -1 : fNavx.getYaw();
		fNavxRoll = fIsInverted.get("roll") ? fNavx.getRoll() * -1 : fNavx.getRoll();
		fNavxPitch = fIsInverted.get("pitch") ? fNavx.getPitch() * -1 : fNavx.getPitch();
		fNavxCompass = fIsInverted.get("compass") ? 360 - fNavx.getCompassHeading() : fNavx.getCompassHeading();
		fNavxAngle = fIsInverted.get("angle") ? fNavx.getAngle() * -1 : fNavx.getAngle();
		fNavxFusedHeading = fIsInverted.get("fused_heading") ? 360 - fNavx.getFusedHeading() : fNavx.getFusedHeading();
		fNavxAccelX = fIsInverted.get("accel_x") ? fNavx.getRawAccelX() * -1 : fNavx.getRawAccelX();
		fNavxAccelY = fIsInverted.get("accel_y") ? fNavx.getRawAccelY() * -1 : fNavx.getRawAccelY();
		fNavxAccelZ = fIsInverted.get("accel_z") ? fNavx.getRawAccelZ() * -1 : fNavx.getRawAccelZ();

		//Radians
		fNavxYaw = fIsRaidans.get("yaw") ? fNavxYaw * Math.PI / 180 : fNavxYaw;
		fNavxRoll = fIsRaidans.get("roll") ? fNavxRoll * Math.PI / 180 : fNavxRoll;
		fNavxPitch = fIsRaidans.get("pitch") ? fNavxPitch * Math.PI / 180 : fNavxPitch;
		fNavxCompass = fIsRaidans.get("compass") ? fNavxCompass * Math.PI / 180 : fNavxCompass;
		fNavxAngle = fIsRaidans.get("angle") ? fNavxAngle * Math.PI / 180 : fNavxAngle;
		fNavxFusedHeading = fIsRaidans.get("fused_heading") ? fNavxFusedHeading * Math.PI / 180 : fNavxFusedHeading;

		return Map.of("yaw", fNavxYaw, "roll", fNavxRoll, "pitch", fNavxPitch, "compass", fNavxCompass, "angle", fNavxAngle, "fused_heading", fNavxFusedHeading, "accel_x", fNavxAccelX, "accel_y", fNavxAccelY, "accel_z", fNavxAccelZ);
	}


}
