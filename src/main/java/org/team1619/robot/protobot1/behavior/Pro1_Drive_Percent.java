package org.team1619.robot.protobot1.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

import java.util.Set;

public class Pro1_Drive_Percent implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Pro1_Drive_Percent.class);
	private static final Set<String> sSubsystems = Set.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;

	private String fXAxis;
	private String fYAxis;
	private String fGearShiftButton;
	private String fStateName;


	public Pro1_Drive_Percent(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;

		fXAxis = robotConfiguration.getString("global_drive", "x");
		fYAxis = robotConfiguration.getString("global_drive", "y");
		fStateName = "Unknown";
		fGearShiftButton = robotConfiguration.getString("global_drive", "gear_shift_button");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
	}

	@Override
	public void update() {
		double xAxis = fSharedInputValues.getNumeric(fXAxis);
		double yAxis = fSharedInputValues.getNumeric(fYAxis);
		boolean gearShiftButton = fSharedInputValues.getBoolean(fGearShiftButton);

		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, yAxis + xAxis, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, yAxis - xAxis, null);
		if (gearShiftButton) {
			fSharedOutputValues.setSolenoidOutputValue("so_drive_gear", true);
		} else {
			fSharedOutputValues.setSolenoidOutputValue("so_drive_gear", false);
		}

	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setSolenoidOutputValue("so_drive_gear", false);
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isRequestingDisposal() {
		return false;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}
