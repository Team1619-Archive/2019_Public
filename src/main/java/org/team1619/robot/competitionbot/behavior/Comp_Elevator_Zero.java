package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import org.team1619.utilities.Timer;
import java.util.Set;

/**
 * Zeros the elevator
 * Then holds the elevator at 0.0 unless the setpoint is changed by the operator
 */

public class Comp_Elevator_Zero implements Behavior {
	private static final Logger sLogger = LogManager.getLogger(Comp_Elevator_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_elevator");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private String fStateName;
	private final int TIMEOUT_TIME;
	private String fLimitSwitch;
	private double fDesiredSetpoint;
	private Timer fTimeoutTimer;

	public Comp_Elevator_Zero(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fStateName = "Unknown";
		fLimitSwitch = robotConfiguration.getString("global_elevator", "limit_switch");
		TIMEOUT_TIME = robotConfiguration.getInt("global_elevator", "zero_timeout_time");
		fTimeoutTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fDesiredSetpoint = config.getDouble("setpoint", 0.0);
		fTimeoutTimer.reset();
		fTimeoutTimer.start(TIMEOUT_TIME);
	}

	@Override
	public void update() {
		if(!fSharedInputValues.getBoolean("bi_elevator_has_been_zeroed")) {
			boolean limitSwitch = fSharedInputValues.getBoolean(fLimitSwitch);
			fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.PERCENT, -0.25, null);

			if (limitSwitch || fTimeoutTimer.isDone()) {
				fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.PERCENT, -0.1, "zero");
				if(Math.abs(fSharedInputValues.getNumeric("mo_elevator_primary_position")) < 0.1) {
					if (limitSwitch) {
						sLogger.info("Elevator Zero -> Zeroed");
					} else {
						sLogger.error("Elevator Zero -> ***** Timed out before zeroing was completed");
					}
					fSharedInputValues.setBoolean("bi_elevator_has_been_zeroed", true);
				}
			}
		} else {
			fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.MOTION_MAGIC, fDesiredSetpoint, "pr_elevator");
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("bi_elevator_has_been_zeroed");

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
