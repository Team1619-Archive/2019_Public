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
 * Autonomously drives the robot at a set speed
 */

public class Comp_Drive_Percent_Auto implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_Percent_Auto.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private double fDriveSpeedLeft;
	private double fDriveSpeedRight;
	private int fDriveTime;
	private final Timer fDriveTimer = new Timer();

	public Comp_Drive_Percent_Auto(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fDriveSpeedLeft = 0.0;
		fDriveSpeedRight = 0.0;
		fDriveTime = 0;


	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fDriveSpeedLeft = config.getDouble("drive_speed_left");
		fDriveSpeedRight = config.getDouble("drive_speed_right");
		fDriveTime = config.getInt("time");
		fDriveTimer.reset();
		fDriveTimer.start(fDriveTime);
	}

	@Override
	public void update() {
		if(!fDriveTimer.isDone()) {
			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, fDriveSpeedLeft, null);
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, fDriveSpeedRight, null);
		} else {
			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
		}
	}


	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
	}

	@Override
	public boolean isDone() {
		return fDriveTimer.isDone();
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