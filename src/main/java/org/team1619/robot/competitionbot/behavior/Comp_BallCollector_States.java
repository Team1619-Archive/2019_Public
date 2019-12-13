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
 * Moves the ball collector arm to the setpoint specified in the state and then idles at that position until interupted by the next state.
 * Reads in the desired position and the beam sensor and determines the speed of the intake rollers
 */

public class Comp_BallCollector_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_BallCollector_States.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_ball_collector");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private final int BALL_COLLECTOR_PID_THRESHOLD;
	private final double MAX_COLLECT_POSITION;

	private final int TIMEOUT_TIME;
	private double fIntakeRollerSpeed;
	private double fEjectRollerSpeed;
	private double fHoldRollerSpeed;
	private double fCurrentRollerSpeed;
	private int fBeamSensorTripTime;
	private double fPercentHold;
	private double fYAxisOffset;

	private String fDesiredPosition;
	private double fSetpoint;
	private double fDesiredSetpoint;
	private String fEncoder;
	private String fYAxis;
	private double fCurrentEncoderPosition;
	private String fCrossbowPosition;
	private boolean fBeamSensorTripped;
	private String fEjectButton;
	private String fBeamSensor;
	private boolean fSetpointReached;
	private boolean fShouldPrint;
	private Timer fBeamSensorTimer;
	private Timer fTimeoutTimer;
	private Timer fReZeroTimer;
	private boolean fTimedOut;
	private boolean fSetpointSetToCurrentEncoderPosition;

	public Comp_BallCollector_States(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fDesiredPosition = "zeroed";
		fSetpoint = 0.0;
		fDesiredSetpoint = 0.0;
		fYAxisOffset = 0.0;
		fCurrentRollerSpeed = 0.0;
		fCurrentEncoderPosition = 0.0;
		fSetpointSetToCurrentEncoderPosition = false;
		fBeamSensorTripped = false;
		fShouldPrint = true;
		fBeamSensorTimer = new Timer();
		fTimeoutTimer = new Timer();
		fReZeroTimer = new Timer();
		fTimedOut = false;

		fEncoder = robotConfiguration.getString("global_ball_collector", "ball_collector_encoder");
		fYAxis = robotConfiguration.getString("global_ball_collector", "pivot_axis");
		fCrossbowPosition = fSharedInputValues.getString("si_crossbow_position");
		MAX_COLLECT_POSITION = robotConfiguration.getDouble("global_ball_collector", "max_collect_position");
		BALL_COLLECTOR_PID_THRESHOLD = robotConfiguration.getInt("global_ball_collector", "pid_threshold");
		TIMEOUT_TIME = robotConfiguration.getInt("global_ball_collector", "timeout_time");
		fEjectButton = robotConfiguration.getString("global_ball_collector", "eject_button");
		fBeamSensor = robotConfiguration.getString("global_ball_collector", "beam_sensor");
		fIntakeRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "intake_roller_speed");
		fEjectRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "eject_roller_speed");
		fHoldRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "hold_roller_speed");
		fBeamSensorTripTime = robotConfiguration.getInt("global_ball_collector", "beam_sensor_trip_time");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;

		fDesiredPosition = config.getString("position");
		fDesiredSetpoint = config.getDouble("setpoint");
		fTimeoutTimer.reset();
		fTimeoutTimer.start(TIMEOUT_TIME);
		fPercentHold = config.getDouble("percent_hold", 0.0);
		fShouldPrint = true;
		fBeamSensorTimer.reset();
		fSetpointReached = false;
		fBeamSensorTripped = false;
		fSharedInputValues.setString("si_ball_collector_current_position", fDesiredPosition);
		fTimedOut = false;
		fReZeroTimer.reset();
		fYAxisOffset = 0.0;
		fSetpointSetToCurrentEncoderPosition = false;
	}

	@Override
	public void update() {
		// Read buttons and sensors on every frame
		boolean ejectButton = fSharedInputValues.getBoolean(fEjectButton);
		boolean beamSensor = fSharedInputValues.getBoolean(fBeamSensor);
		fCrossbowPosition = fSharedInputValues.getString("si_crossbow_position");
		boolean isSafeToMove = fCrossbowPosition.equals("retracted");


		//Ensure beam sensor is triggered for a set time before going to hold
		if (beamSensor) {
			if (!fBeamSensorTimer.isStarted()) {
				fBeamSensorTimer.start(fBeamSensorTripTime);
			} else if (fBeamSensorTimer.isDone()) {
				fBeamSensorTripped = true;
				fBeamSensorTimer.reset();
			}
		} else {
			fBeamSensorTimer.reset();
			fBeamSensorTripped = false;
		}

		// Update intake motor speed
		if ((ejectButton && beamSensor)) {
			fCurrentRollerSpeed = fEjectRollerSpeed;
		} else if (fDesiredPosition.contains("collect")) {
			fCurrentRollerSpeed = fIntakeRollerSpeed;
		} else if (beamSensor) {
			fCurrentRollerSpeed = fHoldRollerSpeed;
		} else {
			fCurrentRollerSpeed = 0.0;
		}
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_roller", Motor.OutputType.PERCENT, fCurrentRollerSpeed, null);

		//Check if setpoint has been reached and then set percent hold if collecting balls
		fCurrentEncoderPosition = fSharedInputValues.getNumeric(fEncoder);

		//Set the setpoint based on weather it's safe to move or has timed out
		if(!isSafeToMove) {
			if(!fSetpointSetToCurrentEncoderPosition) {
				fSetpoint = fCurrentEncoderPosition;
				fSetpointSetToCurrentEncoderPosition = true;
				sLogger.info("Ball Collector -> Not safe to move, setpoint set to current encoder position");
			}
			fYAxisOffset = fSharedInputValues.getNumeric(fYAxis);
			fTimeoutTimer.reset();
			fTimeoutTimer.start(TIMEOUT_TIME);
		} else if (fTimedOut) {
			if(!fSetpointSetToCurrentEncoderPosition) {
				fSetpoint = fCurrentEncoderPosition;
				fSetpointSetToCurrentEncoderPosition = true;
				sLogger.info("Ball Collector -> timed out, setpoint set to current encoder position");
			}
			fYAxisOffset = fSharedInputValues.getNumeric(fYAxis);
		} else {
			fSetpointSetToCurrentEncoderPosition = false;
			fSetpoint = fDesiredSetpoint;
			fYAxisOffset = fYAxisOffset + fSharedInputValues.getNumeric(fYAxis);
		}
		//Add the yAxis to the setpoint and have the value persist between frames
		fSetpoint = fSetpoint + fYAxisOffset;


		//Determines when we reach the desired setpoint reached
		if(!fSetpointReached){
			fSetpointReached = Math.abs(fCurrentEncoderPosition - fSetpoint) < BALL_COLLECTOR_PID_THRESHOLD;
			if (fTimeoutTimer.isDone()) {
				fTimedOut = true;
				fSetpointReached = true;
				sLogger.info("****** Ball Collector Timed out - setpoint not reached *******");
			}
		}
		if(!fSetpointReached && !fTimedOut){
			if (fSetpoint > fCurrentEncoderPosition) {
				fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.MOTION_MAGIC, fSetpoint, "pr_down");
			} else if (fSetpoint < fCurrentEncoderPosition) {
				fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.MOTION_MAGIC, fSetpoint, "pr_up");
			}
		}else if((Math.abs(fPercentHold) > 0.0) && (fCurrentEncoderPosition < MAX_COLLECT_POSITION) && !fTimedOut){
			fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, fPercentHold, null);
		}else{
			fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.MOTION_MAGIC, fSetpoint, "pr_idle");
		}

		if (fShouldPrint) {
			print();
			fShouldPrint = false;
		}

		fSharedInputValues.setNumeric("ni_ball_collector_setpoint", fSetpoint);

	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_roller", Motor.OutputType.PERCENT, 0.0, null);
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


	private void print() {
		sLogger.debug("Ball Collector Set -> Desired Position = {}, Pivot Motor Setpoint = {}, Roller Speed = {}", fDesiredPosition, fSharedOutputValues.getMotorOutputValue("mo_ball_collector_pivot"), fCurrentRollerSpeed);
	}

}
