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
 * Runs the climber to a set position then cuts the power until the climber falls below/ rises above that position then returns power
 * Sets the vacuum pump speed
 */

public class Comp_Climber_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Climber_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_climber");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private String fDesiredPosition;
	private double fDesiredVacuumPumpSpeed;
	private double fManualSpeed;
	private double MIN_VACUUM_THRESHOLD;
	private double MAX_VACUUM_THRESHOLD;
	private double SAFE_TO_CLIMB_VACUUM_LEVEL;

	private String fClimberEncoder;
	private double fCurrentEncoderPosition;
	private String fButtonUp;
	private String fButtonDown;
	private boolean fIsFinished;
	private String fVacuumPumpButton;
	private String fVacuumSensor;
	private double fVacuumPumpSpeed;
	private double fSetpoint;
	private double fDesiredSetpoint;
	private Timer fTimer;
	private boolean fShouldPrint;
	private boolean fVacuumPumpEnabled;
	private boolean fVacuumWasBelowMin;
	private final double ERROR_THRESHOLD;
	private final int ERROR_TIMER_LENGTH;

	private String fClimbStage;

	public Comp_Climber_States(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";

		fDesiredPosition = "unknown";
		fSetpoint = 0.0;
		fDesiredSetpoint = 0.0;
		fCurrentEncoderPosition = 0.0;
		fVacuumPumpSpeed = 0.0;
		fVacuumWasBelowMin = false;
		fVacuumPumpEnabled = false;
		fIsFinished = false;
		fShouldPrint = true;
		fTimer = new Timer();
		fClimbStage = "zeroed";

		fClimberEncoder = robotConfiguration.getString("global_climber", "climber_encoder");
		fButtonUp = robotConfiguration.getString("global_climber", "button_up");
		fButtonDown = robotConfiguration.getString("global_climber", "button_down");
		fVacuumPumpButton = robotConfiguration.getString("global_climber", "vacuum_pump_button");
		fVacuumSensor = robotConfiguration.getString("global_climber", "vacuum_sensor");
		fManualSpeed = robotConfiguration.getDouble("global_climber", "manual_speed");
		fDesiredVacuumPumpSpeed = robotConfiguration.getDouble("global_climber", "vacuum_pump_speed");
		MIN_VACUUM_THRESHOLD = robotConfiguration.getDouble("global_climber", "min_vacuum_threshold");
		MAX_VACUUM_THRESHOLD = robotConfiguration.getDouble("global_climber", "max_vacuum_threshold");
		SAFE_TO_CLIMB_VACUUM_LEVEL = robotConfiguration.getDouble("global_climber", "safe_to_climb_vacuum_level");
		ERROR_THRESHOLD = robotConfiguration.getDouble("global_climber", "error_threshold");
		ERROR_TIMER_LENGTH = robotConfiguration.getInt("global_climber", "error_timer_length");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fDesiredPosition = config.getString("position");
		fDesiredSetpoint = config.getDouble("setpoint", 0.0);
		fClimbStage = fSharedInputValues.getString("si_climb_stage");
		fShouldPrint = true;
		fIsFinished = false;
		fTimer.reset();
		fSharedOutputValues.setMotorOutputValue("mo_driver_right_rumble", Motor.OutputType.RUMBLE, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_driver_left_rumble", Motor.OutputType.RUMBLE, 0.0, null);
	}

	@Override
	public void update() {
		fCurrentEncoderPosition = fSharedInputValues.getNumeric(fClimberEncoder, null);
		boolean buttonUp = fSharedInputValues.getBoolean(fButtonUp, null);
		boolean buttonDown = fSharedInputValues.getBoolean(fButtonDown, null);
		boolean vacuumPumpButton = fSharedInputValues.getBoolean(fVacuumPumpButton, null);
		double vacuumSensor = fSharedInputValues.getNumeric(fVacuumSensor, null);
		boolean climbEnable = fSharedInputValues.getBoolean("bi_climb_enabled", null);

		// Only execute if enabled
		if (climbEnable) {

			//Set Climb motor
			double manualSpeed = 0.0;
			String motorProfile = "";
			switch (fDesiredPosition){
				case "deploy":
					motorProfile = "pr_deploy";
					if(fClimbStage.equals("zeroed") || fClimbStage.equals("deployed")) {
						fSetpoint = fDesiredSetpoint;
					} else {
						fSetpoint = fCurrentEncoderPosition;
					}

					if (Math.abs(fDesiredSetpoint - fCurrentEncoderPosition) <= ERROR_THRESHOLD && !fIsFinished && !fTimer.isStarted()) {
						fTimer.start(ERROR_TIMER_LENGTH);
					} else if (!fIsFinished && fTimer.isDone()) {
						fIsFinished = true;
						fClimbStage = "deployed";
					}
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.MOTION_MAGIC, fSetpoint, motorProfile);

					break;
				case "lock_on":
					motorProfile = "pr_lock_on";
					if(fClimbStage.equals("deployed") || fClimbStage.equals("locked_on")) {
						fSetpoint = fDesiredSetpoint;
						fVacuumPumpEnabled = true;
						fSharedInputValues.setBoolean("bi_vacuum_pump_enabled", true);
					} else {
						fSetpoint = fCurrentEncoderPosition;
					}

					if (Math.abs(fDesiredSetpoint - fCurrentEncoderPosition) <= ERROR_THRESHOLD && !fIsFinished && !fTimer.isStarted()) {
						fTimer.start(ERROR_TIMER_LENGTH);
					} else if (!fIsFinished && fTimer.isDone()) {
						fIsFinished = true;
						fClimbStage = "locked_on";
					}
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.MOTION_MAGIC, fSetpoint, motorProfile);

					break;
				case "climb":
					motorProfile = "pr_climb";
					if((fClimbStage.equals("locked_on") || fClimbStage.equals("climbed")) && vacuumSensor >= SAFE_TO_CLIMB_VACUUM_LEVEL) {
						fSetpoint = fDesiredSetpoint;
					} else {
						fSetpoint = fCurrentEncoderPosition;
					}

					if (Math.abs(fDesiredSetpoint - fCurrentEncoderPosition) <= ERROR_THRESHOLD && !fIsFinished && !fTimer.isStarted()) {
						fTimer.start(ERROR_TIMER_LENGTH);
					} else if (!fIsFinished && fTimer.isDone()) {
						fIsFinished = true;
						fClimbStage = "climbed";
					}
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.MOTION_MAGIC, fSetpoint, motorProfile);

					break;
				case "manual_override":
					//Manual adjustment
					if (buttonUp) {
						manualSpeed = fManualSpeed;
					} else if (buttonDown) {
						manualSpeed = -fManualSpeed;
					} else {
						manualSpeed = 0;
					}
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, manualSpeed, null);
					break;
				case "idle":
					manualSpeed = 0;
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, manualSpeed, null);
					break;
				default:
					manualSpeed = 0;
					fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, manualSpeed, null);
					sLogger.info("Unknown Climber State");
					break;
			}


			//Set vacuum pump speed so it cycles between a min and max
			if (!fSharedInputValues.getBoolean("bi_vacuum_pump_enabled", null)){
				fVacuumPumpEnabled = false;
			}
			if (fVacuumPumpEnabled || vacuumPumpButton) {
				if (vacuumSensor < MIN_VACUUM_THRESHOLD) {
					fVacuumPumpSpeed = fDesiredVacuumPumpSpeed;
					fVacuumWasBelowMin = true;
				} else if (vacuumSensor > MAX_VACUUM_THRESHOLD) {
					fVacuumPumpSpeed = 0.0;
					fVacuumWasBelowMin = false;
				} else {
					if (fVacuumWasBelowMin) {
						fVacuumPumpSpeed = fDesiredVacuumPumpSpeed;
					} else {
						fVacuumPumpSpeed = 0.0;
					}
				}
			} else {
				fVacuumPumpSpeed = 0.0;
			}
			fSharedOutputValues.setMotorOutputValue("mo_vacuum_pump", Motor.OutputType.PERCENT, fVacuumPumpSpeed, null);

			//Rumble controllers if there is enough vacuum
			if (vacuumSensor > SAFE_TO_CLIMB_VACUUM_LEVEL && fDesiredPosition.equals("lock_on")) {
				fSharedOutputValues.setMotorOutputValue("mo_driver_right_rumble", Motor.OutputType.RUMBLE, 1.0, null);
				fSharedOutputValues.setMotorOutputValue("mo_driver_left_rumble", Motor.OutputType.RUMBLE, 1.0, null);
			} else {
				fSharedOutputValues.setMotorOutputValue("mo_driver_right_rumble", Motor.OutputType.RUMBLE, 0.0, null);
				fSharedOutputValues.setMotorOutputValue("mo_driver_left_rumble", Motor.OutputType.RUMBLE, 0.0, null);
			}

			if (fShouldPrint) {
				print();
				fShouldPrint = false;
			}
		}
		fSharedInputValues.setNumeric("ni_climber_setpoint", fSetpoint);
		fSharedInputValues.setString("si_climb_stage", fClimbStage);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_climber_group", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_driver_right_rumble", Motor.OutputType.RUMBLE, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_driver_left_rumble", Motor.OutputType.RUMBLE, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_vacuum_pump", Motor.OutputType.PERCENT, 0.0, null);
	}

	@Override
	public boolean isDone() {
		return fIsFinished;
	}

	@Override
	public boolean isRequestingDisposal() {
		return false;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}

	private void print() {
		sLogger.debug("Climber Set -> Desired Position = {}, Motor Speed = {}, Vacuum Pump Speed = {}, Vacuum Sensor = {}", fDesiredPosition, fSharedOutputValues.getMotorOutputs("mo_climber_group").get("value"), fVacuumPumpSpeed, fSharedInputValues.getNumeric(fVacuumSensor, null));
	}

}
