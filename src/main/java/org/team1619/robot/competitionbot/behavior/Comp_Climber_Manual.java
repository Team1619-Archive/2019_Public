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
 * Allows you to run the climber motors manually
 */

public class Comp_Climber_Manual implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Climber_Manual.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_climber");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private final String fButtonUp;
	private final String fButtonDown;
	private boolean fClimbEnable;
	private String fVacuumPumpButton;
	private double fManualSpeed;
	private double fDesiredVacuumPumpSpeed;


	public Comp_Climber_Manual(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";

		fButtonUp = robotConfiguration.getString("global_climber", "button_up");
		fButtonDown = robotConfiguration.getString("global_climber", "button_down");
		fVacuumPumpButton = robotConfiguration.getString("global_climber", "vacuum_pump_button");
		fManualSpeed = robotConfiguration.getDouble("global_climber", "manual_speed");
		fDesiredVacuumPumpSpeed = robotConfiguration.getDouble("global_climber", "vacuum_pump_speed");
		fClimbEnable = false;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
	}

	@Override
	public void update() {
		boolean up = fSharedInputValues.getBoolean(fButtonUp, null);
		boolean down = fSharedInputValues.getBoolean(fButtonDown, null);
		boolean vacuumPumpButton = fSharedInputValues.getBoolean(fVacuumPumpButton, null);
		fClimbEnable = fSharedInputValues.getBoolean("bi_climb_enabled", null);
		double motorOutputValue;
		double vacuumPumpOutputValue;

		//Only execute if the climb is enabled
		if (fClimbEnable) {

			//Set climb motor
			if (up) {
				motorOutputValue = fManualSpeed;
			} else if (down) {
				motorOutputValue = -fManualSpeed;
			} else {
				motorOutputValue = 0.0;
			}
			fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, motorOutputValue, null);

			//Set vacuum pump
			if (vacuumPumpButton) {
				vacuumPumpOutputValue = fDesiredVacuumPumpSpeed;
			} else {
				vacuumPumpOutputValue = 0.0;
			}
			fSharedOutputValues.setMotorOutputValue("mo_vacuum_pump", Motor.OutputType.PERCENT, vacuumPumpOutputValue, null);

		}
	}


	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_vacuum_pump", Motor.OutputType.PERCENT, 0.0, null);

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
	public ImmutableSet<String> getSubsystems() {
		return sSubsystems;
	}

}
