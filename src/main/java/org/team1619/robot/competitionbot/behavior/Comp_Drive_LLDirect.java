package org.team1619.robot.competitionbot.behavior;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

/**
 * Aligns the robot with a target detected by the limelight
 */

public class Comp_Drive_LLDirect implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_LLDirect.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;

	private String fYAxis;
	private String fLimeLightAdjustLeftButton;
	private String fLimeLightAdjustRightButton;
	private String fStateName;
	private String fRightRumble;
	private String fLeftRumble;
	private double fTrackWidth;
	private double fDriveSpeedScale;

	public Comp_Drive_LLDirect(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fYAxis = robotConfiguration.getString("global_drive", "y");
		fLimeLightAdjustLeftButton = robotConfiguration.getString("global_drive", "ll_adjust_left");
		fLimeLightAdjustRightButton = robotConfiguration.getString("global_drive", "ll_adjust_right");
		fRightRumble = robotConfiguration.getString("global_drive", "right_rumble");
		fLeftRumble = robotConfiguration.getString("global_drive", "left_rumble");
		fTrackWidth = robotConfiguration.getDouble("global_drive", "lldirect_track_width");
		fDriveSpeedScale = 1.0;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;

		fDriveSpeedScale = config.getDouble("drive_speed_scale");
	}

	@Override
	public void update() {

		double velocity = fSharedInputValues.getNumeric(fYAxis, null) * fDriveSpeedScale;

		double leftOutput = 0;
		double rightOutput = 0;

		double xOffset = 0.0;

		// Calculates the distance from the robot to the target, based on the targets area
		if (fSharedInputValues.getBoolean("bi_is_top_limelight")) {
			if (fSharedInputValues.getBooleanRisingEdge(fLimeLightAdjustLeftButton)) {
				fSharedInputValues.setNumeric("ni_top_limelight_offset", fSharedInputValues.getNumeric("ni_top_limelight_offset") - 0.02);
			} else if (fSharedInputValues.getBooleanRisingEdge(fLimeLightAdjustRightButton)) {
				fSharedInputValues.setNumeric("ni_top_limelight_offset", fSharedInputValues.getNumeric("ni_top_limelight_offset") + 0.02);
			}
			fSharedInputValues.setNumeric("ni_ll_distance", Math.pow(2, -((fSharedInputValues.getVector("vi_limelight_top").get("ta") - 6) / 2.4)));
			xOffset = getRobotXOffset(fSharedInputValues.getVector("vi_limelight_top", null).get("tx")) + fSharedInputValues.getNumeric("ni_top_limelight_offset");
		} else {
			if (fSharedInputValues.getBooleanRisingEdge(fLimeLightAdjustLeftButton)) {
				fSharedInputValues.setNumeric("ni_bottom_limelight_offset", fSharedInputValues.getNumeric("ni_bottom_limelight_offset") - 0.02);
			} else if (fSharedInputValues.getBooleanRisingEdge(fLimeLightAdjustRightButton)) {
				fSharedInputValues.setNumeric("ni_bottom_limelight_offset", fSharedInputValues.getNumeric("ni_bottom_limelight_offset") + 0.02);
			}
			//fSharedInputValues.setNumeric("ni_ll_distance", Math.pow(2, -((fSharedInputValues.getVector("vi_limelight_bottom").get("ta") - 4.2))));
			fSharedInputValues.setNumeric("ni_ll_distance", 1.5);
			xOffset = getRobotXOffset(fSharedInputValues.getVector("vi_limelight_bottom", null).get("tx")) + fSharedInputValues.getNumeric("ni_bottom_limelight_offset");
		}

		double distance = fSharedInputValues.getNumeric("ni_ll_distance");

		if (distance < 0.5) distance = 0.5;

		// Calculates the curvature to the target, using the target x offset and distance
		double curvature = xOffset / distance;

		fSharedInputValues.setNumeric("ni_ll_curvature", curvature);

		// Calculates wheel outputs based on curvature and velocity values
		if (velocity < 0) {
			leftOutput = (velocity * ((1.5 - curvature * fTrackWidth) / 1.5));
			rightOutput = (velocity * ((1.5 + curvature * fTrackWidth) / 1.5));
		} else {
			leftOutput = (velocity * ((1.5 + curvature * fTrackWidth) / 1.5));
			rightOutput = (velocity * ((1.5 - curvature * fTrackWidth) / 1.5));
		}

		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, leftOutput, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, rightOutput, null);

		fSharedOutputValues.setSolenoidOutputValue("so_gear_shifter", true);
		fSharedInputValues.setBoolean("bi_is_low_gear", true);

		fSharedOutputValues.setMotorOutputValue(fRightRumble, Motor.OutputType.RUMBLE, 1.0, null);
		fSharedOutputValues.setMotorOutputValue(fLeftRumble, Motor.OutputType.RUMBLE, 1.0, null);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue(fRightRumble, Motor.OutputType.RUMBLE, 0.0, null);
		fSharedOutputValues.setMotorOutputValue(fLeftRumble, Motor.OutputType.RUMBLE, 0.0, null);
		fSharedInputValues.setBoolean("bi_leaving_ll_direct", true);
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isRequestingDisposal() {
		return isDone();
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		return sSubsystems;
	}

	private double getRobotXOffset(double limelightXOffset) {
		return limelightXOffset;
	}
}
