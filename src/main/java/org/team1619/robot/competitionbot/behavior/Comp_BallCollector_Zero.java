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
import org.team1619.utilities.Timer;
import java.util.Set;

/**
 * Zeros the ball collector
 * Holds at a set encoder value until the state is interrupted
 */

public class Comp_BallCollector_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_BallCollector_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_ball_collector");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private final double ZERO_VELOCITY_ERROR_THRESHOLD;

	private String fBallCollectorVelocitySensor;
	private double fSetpoint;
	private double fZeroSpeed;
	private double fHoldSpeed;
	private Timer fTimer;

	public Comp_BallCollector_Zero(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fSetpoint = 0.0;
		fBallCollectorVelocitySensor = robotConfiguration.getString("global_ball_collector", "ball_collector_velocity");
		ZERO_VELOCITY_ERROR_THRESHOLD = robotConfiguration.getDouble("global_ball_collector", "zero_velocity_error_threshold");
		fTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fTimer.reset();
		fSetpoint = config.getDouble("setpoint", 5.0);
		fZeroSpeed = config.getDouble("zero_speed", 0.0);
		fHoldSpeed = config.getDouble("hold_zero", 0.0);

		fSharedInputValues.setNumeric("ni_ball_collector_setpoint", fSetpoint);
		fSharedInputValues.setString("si_ball_collector_current_position", "protect");
	}

	@Override
	public void update() {
		double velocity = fSharedInputValues.getNumeric(fBallCollectorVelocitySensor);

		if(!fSharedInputValues.getBoolean("bi_ball_collector_has_been_zeroed", null)) {
			fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, fZeroSpeed, null);

			if (!fTimer.isStarted() && velocity < ZERO_VELOCITY_ERROR_THRESHOLD) {
				fTimer.start(500);
			} else if (velocity >= ZERO_VELOCITY_ERROR_THRESHOLD) {
				fTimer.reset();
			}

			if (fTimer.isDone()) {
				fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, fHoldSpeed, "zero");
				if(Math.abs(fSharedInputValues.getNumeric("ni_ball_collector_pivot_position", null)) < 0.5){
					fSharedInputValues.setBoolean("bi_ball_collector_has_been_zeroed", true);
					sLogger.info("Ball Collector Zero -> Zeroed");
				}
			}
		} else {
			fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.MOTION_MAGIC, fSetpoint, "pr_idle");
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, 0.0, null);
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("bi_ball_collector_has_been_zeroed", null);
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
