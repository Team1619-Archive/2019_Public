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
 * Zeros the climber
 */

public class Comp_Drive_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_Zero.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;
	private Timer fTimer;
	boolean fIsZeroed;


	public Comp_Drive_Zero(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fTimer = new Timer();
		fIsZeroed = false;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fTimer.reset();
		fIsZeroed = false;
		fTimer.start(500);
	}

	@Override
	public void update() {

		if (!fSharedInputValues.getBoolean("bi_drive_has_been_zeroed", null)) {

			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, "zero");
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, "zero");

			double leftPosition = fSharedInputValues.getNumeric("ni_left_drive_primary_position", null);
			double rightPosition = fSharedInputValues.getNumeric("ni_right_drive_primary_position", null);
			fIsZeroed = Math.abs(leftPosition) < 0.1 && Math.abs(rightPosition) < 0.1;

			if (fIsZeroed || fTimer.isDone()) {
				fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0, null);
				fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0, null);
				fSharedInputValues.setBoolean("bi_odometry_has_been_zeroed", false);
				fSharedInputValues.setBoolean("bi_drive_has_been_zeroed", true);
				if (fIsZeroed) {
					sLogger.info("Drive Zero -> Zeroed");
				} else {
					sLogger.info("Drive Zero -> Timed out before zeroing was completed");
					fIsZeroed = true;
				}
			}
		} else {
			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0, null);
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0, null);
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0, null);
	}

	@Override
	public boolean isDone() {
		return fIsZeroed;
	}

	@Override
	public boolean isRequestingDisposal() {
		return isDone();
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		return sSubsystems;
	}

}
