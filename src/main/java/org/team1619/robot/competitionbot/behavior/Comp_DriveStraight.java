package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.ClosedLoopController;
import org.team1619.utilities.Config;
import java.util.Set;

/**
 * Uses the navx to drive the robot in a straight line
 */

public class Comp_DriveStraight implements Behavior {
	private static final Logger sLogger = LogManager.getLogger(Comp_DriveStraight.class);
	private static final Set<String> sSubsystems = Set.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private final ClosedLoopController fCLosedLoopController = new ClosedLoopController("drive");

	private String fDriveAxis;


	public Comp_DriveStraight(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fDriveAxis = "Unknown";

	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fDriveAxis = config.getString("axis");

		double currentHeading = fSharedInputValues.getVector("vi_navx").get("angle");
		fCLosedLoopController.set(currentHeading);

	}

	@Override
	public void update() {

		double output = fSharedInputValues.getNumeric(fDriveAxis);

		fCLosedLoopController.setProfile("drive_straight");
		double adjustment = fCLosedLoopController.getWithPID(fSharedInputValues.getVector("vi_navx").get("angle"));

		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, output + adjustment, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, output - adjustment, null);
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
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
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}
