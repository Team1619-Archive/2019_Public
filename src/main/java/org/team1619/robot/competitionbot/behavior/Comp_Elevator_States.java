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
 * Reads in a setpoint and calculates a velocity trajectory to get to it
 * Holds the elevator at a setpoint using PID
 */

public class Comp_Elevator_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Elevator_States.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_elevator");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;


	private String fEncoder;
	private double fDesiredSetpoint;
	private String fDesiredPosition;
	private String fYAxis;
	private final double ERROR_THRESHOLD;
	private final double MAX_HEIGHT;
	private final int MOTION_MAGIC_TIMEOUT;
	private Timer fTimeoutTimer;
	private boolean fSetpointReached;
	private double fCurrentEncoderPosition;


	public Comp_Elevator_States(InputValues inputValues, OutputValues outputValues, Config config, RobotConfiguration robotConfiguration, Dashboard dashboard) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fDesiredPosition = "zeroed";
		fCurrentEncoderPosition = 0.0;
		fTimeoutTimer = new Timer();

		fEncoder = robotConfiguration.getString("global_elevator", "encoder");
		fYAxis = robotConfiguration.getString("global_elevator", "y_axis");
		ERROR_THRESHOLD = robotConfiguration.getDouble("global_elevator", "error_threshold");
		MAX_HEIGHT = robotConfiguration.getDouble("global_elevator", "elevator_max_height");
		MOTION_MAGIC_TIMEOUT = robotConfiguration.getInt("global_elevator", "motion_magic_timeout");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fTimeoutTimer.reset();
		fDesiredPosition = config.getString("position");
		fDesiredSetpoint = config.getDouble("setpoint");
		fTimeoutTimer.start(MOTION_MAGIC_TIMEOUT);
		fSharedInputValues.setNumeric("ni_elevator_setpoint", fDesiredSetpoint);
		fSetpointReached = false;

	}

	@Override
	public void update() {

		//manually adjust the setpoint but keep it within the elevator's limits
		if (!fDesiredPosition.equals("protect")) {
			fDesiredSetpoint += fSharedInputValues.getNumeric(fYAxis, null);
		}
		if (fDesiredSetpoint > MAX_HEIGHT) {
			fDesiredSetpoint = MAX_HEIGHT;
		}
		if (fDesiredSetpoint < 0.0) {
			fDesiredSetpoint = 0.0;
		}

		fCurrentEncoderPosition = fSharedInputValues.getNumeric(fEncoder);

		//Determine if the setpoint has been reached
		if(!fSetpointReached){
			fSetpointReached = Math.abs(fCurrentEncoderPosition - fDesiredSetpoint) < ERROR_THRESHOLD;
			if (fTimeoutTimer.isDone()) {
				fSetpointReached = true;
				sLogger.info("****** Elevator Timed out - setpoint not reached *******");
				fDesiredSetpoint = fCurrentEncoderPosition;
			}
		}

		fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.MOTION_MAGIC, fDesiredSetpoint, "pr_elevator");

		fSharedInputValues.setNumeric("ni_elevator_setpoint", fDesiredSetpoint);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_elevator_group", Motor.OutputType.PERCENT, 0.0, null);
	}

	@Override
	public boolean isDone() {
		return fSetpointReached;
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
