package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import java.util.Set;

/**
 * Zeros the climber
 */

public class Comp_Climber_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Climber_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_climber");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private String fClimberEncoder;
	private String fClimberVelocitySensor;

//	public Comp_Climber_States.ClimbStage fClimbStage;

	public Comp_Climber_Zero(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";

		fClimberEncoder = robotConfiguration.getString("global_climber", "climber_encoder");
		fClimberVelocitySensor = robotConfiguration.getString("global_climber", "climber_velocity");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fSharedInputValues.setNumeric("ni_climber_setpoint", 0.0);
	}

	@Override
	public void update() {
		if(!fSharedInputValues.getBoolean("bi_climber_has_been_zeroed", null)) {
			fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, 0.0, "zero");
			if(Math.abs(fSharedInputValues.getNumeric("ni_climber_master_position", null)) <0.1){
				sLogger.info("Climber Zero -> Zeroed");
				fSharedInputValues.setString("si_climb_stage", "zeroed");
				fSharedInputValues.setBoolean("bi_climber_has_been_zeroed", true);
			}
		} else {
			fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, 0.0, null);
		}
	}

	@Override
	public void dispose() {
		fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, 0.0, null);
		sLogger.debug("Leaving state {}", fStateName);
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("bi_climber_has_been_zeroed", null);
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
