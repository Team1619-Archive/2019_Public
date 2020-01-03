package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import java.util.Set;

/**
 * Manually drives the elevator motors
 */

public class Comp_Elevator_Manual implements Behavior {
	private static final Logger sLogger = LogManager.getLogger(Comp_Elevator_Manual.class);
	private static final Set<String> sSubsystems = Set.of("ss_elevator");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private String fStateName;
	private String fYAxis;

	public Comp_Elevator_Manual(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fStateName = "Unknown";
		fYAxis = robotConfiguration.getString("global_elevator", "y_axis");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
	}

	@Override
	public void update() {
		double output = fSharedInputValues.getNumeric(fYAxis);
		fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.PERCENT, output, null);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.PERCENT, 0.0, null);
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
