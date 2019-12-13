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
import org.team1619.utilities.Timer;

/**
 * Aligns the robot with a target detected by the limelight automatically
 */

public class Comp_Drive_LLDirect_Auto implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_LLDirect_Auto.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;

	private Timer fCloseTimer;
	private Timer fFinishedTimer;
	private String fStateName;
	private double fDriveSpeed;
	private double fCloseDistance;
	private double fTrackWidth;

	public Comp_Drive_LLDirect_Auto(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fTrackWidth = robotConfiguration.getDouble("global_drive", "lldirect_track_width");
		fCloseTimer = new Timer();
		fFinishedTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fDriveSpeed = config.getDouble("drive_speed", 0.0);
		fCloseDistance = config.getDouble("close_distance", 0.0);

		fCloseTimer.reset();
		fFinishedTimer.reset();

		fSharedInputValues.setBoolean("bi_is_top_limelight", !(fSharedInputValues.getNumeric("ni_elevator_primary_position") > 20.0 && fSharedInputValues.getNumeric("ni_elevator_primary_position") < 40.0));

		if (fSharedInputValues.getBoolean("bi_is_top_limelight")) {
			fDashboard.setNetworkTableValue("limelight-top", "ledMode", 0);
			fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 1);
		} else {
			fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 0);
			fDashboard.setNetworkTableValue("limelight-top", "ledMode", 1);
		}
	}

	@Override
	public void update() {

		double distance = 0.0;

		double xOffset;
		// Calculates the distance from the robot to the target, based on the targets area
	    if(fSharedInputValues.getBoolean("bi_is_top_limelight")) {
			fSharedInputValues.setNumeric("ni_ll_distance", Math.pow(2, -((fSharedInputValues.getVector("vi_limelight_top").get("ta") - 6) / 2.4)) + 0.3);
		    distance = Math.pow(2, -((fSharedInputValues.getVector("vi_limelight_top").get("ta") - 6) / 2.4));
			xOffset = getRobotXOffset(fSharedInputValues.getVector("vi_limelight_top", null).get("tx"));
	    } else {
		    fSharedInputValues.setNumeric("ni_ll_distance", Math.pow(2, -((fSharedInputValues.getVector("vi_limelight_bottom").get("ta") - 4.2))) + 1.0);
		    distance = 1.5;
		    xOffset = getRobotXOffset(fSharedInputValues.getVector("vi_limelight_bottom", null).get("tx"));

	    }

		double velocity = fDriveSpeed;
		double leftOutput = 0;
		double rightOutput = 0;

		// Calculates the curvature to the target, using the target x offset and distance
		double curvature = xOffset / distance;

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

		if (fSharedInputValues.getNumeric("ni_ll_distance") != 0.0 && fSharedInputValues.getNumeric("ni_ll_distance") < fCloseDistance && !fCloseTimer.isStarted()) {
			fCloseTimer.start(100);
		} else if (fSharedInputValues.getNumeric("ni_ll_distance") > fCloseDistance && fCloseTimer.isStarted()) {
			fCloseTimer.reset();
		}

		if (fCloseTimer.isDone() && !fFinishedTimer.isStarted()) {
			fFinishedTimer.start(500);
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 1);
		fDashboard.setNetworkTableValue("limelight-top", "ledMode", 1);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
		fSharedInputValues.setBoolean("bi_leaving_ll_direct", true);
	}

	@Override
	public boolean isDone() {
		return fFinishedTimer.isDone();
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
