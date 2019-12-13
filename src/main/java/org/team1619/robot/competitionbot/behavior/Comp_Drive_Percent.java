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
 * Drives the robot based on the joystick values
 */

public class Comp_Drive_Percent implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_Percent.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private final int GEAR_SHIFT_DELAY_TIME;
	private String fXAxis;
	private String fYAxis;
	private String fReduceTurnSpeedButton;
	private String fGearShiftButton;
	private double fXAxisScale;
	private double fElevatorMax;
	private double fElevatorMinRampingOutput;
	private Timer fGearshiftTimer;

	public Comp_Drive_Percent(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fXAxis = robotConfiguration.getString("global_drive", "x");
		fYAxis = robotConfiguration.getString("global_drive", "y");
		fXAxisScale = config.getDouble("x_axis_scale");
		fReduceTurnSpeedButton = config.getString("reduce_turn_speed_button");
		fGearShiftButton = robotConfiguration.getString("global_drive", "gear_shift_button");
		GEAR_SHIFT_DELAY_TIME = robotConfiguration.getInt("global_drive", "gear_shift_delay");
		fElevatorMax = robotConfiguration.getDouble("global_elevator", "elevator_max_height");
		fElevatorMinRampingOutput = robotConfiguration.getDouble("global_drive", "elevator_min_ramping_output");
		fGearshiftTimer = new Timer();
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fGearshiftTimer.reset();
		if(fSharedInputValues.getBoolean("bi_leaving_ll_direct")) {
			fSharedInputValues.setBoolean("bi_leaving_ll_direct", false);
			fGearshiftTimer.start(GEAR_SHIFT_DELAY_TIME);
		}
	}

	@Override
	public void update() {
		double xAxis = fSharedInputValues.getNumeric(fXAxis);
		double yAxis = fSharedInputValues.getNumeric(fYAxis);
		boolean buttonScaleButtonValue = fSharedInputValues.getBoolean(fReduceTurnSpeedButton);
		boolean gearShiftButtonValue = fSharedInputValues.getBoolean(fGearShiftButton);

		// If the reduceTurnSpeed button in being held scale the xAxis down for a slower turn
		if (buttonScaleButtonValue) {
			xAxis = xAxis * fXAxisScale;
		}

		// Set the motor speed to the joystick values
		double leftMotorSpeed = yAxis + xAxis;
		double rightMotorSpeed = yAxis - xAxis;

		//Because the joystick values combined can exceed the range the motors except (-1 to 1) limit them to with in that range
		if(leftMotorSpeed > 1){
			rightMotorSpeed = rightMotorSpeed - (leftMotorSpeed - 1);
			leftMotorSpeed = 1;
		}else if (leftMotorSpeed < -1){
			rightMotorSpeed = rightMotorSpeed - (1 + leftMotorSpeed);
			leftMotorSpeed = -1;
		}else if (rightMotorSpeed > 1){
			leftMotorSpeed = leftMotorSpeed - (rightMotorSpeed - 1);
			rightMotorSpeed = 1;
		} else if(rightMotorSpeed < -1){
			leftMotorSpeed = leftMotorSpeed - (1 + rightMotorSpeed);
			rightMotorSpeed = -1;
		}

		// Scale the drive speed by how high the elevator is to prevent tipping over
		double elevatorRamping = fElevatorMinRampingOutput + ((1 - fElevatorMinRampingOutput) * (1 - fSharedInputValues.getNumeric("ni_elevator_primary_position") / fElevatorMax));
		//Todo elevator ramping causes a faster turn speed comparatively than regular driving
		leftMotorSpeed = leftMotorSpeed * elevatorRamping;
		rightMotorSpeed = rightMotorSpeed * elevatorRamping;

		// Set the motors
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, leftMotorSpeed, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, rightMotorSpeed, null);

		if(fGearshiftTimer.isDone() || !fGearshiftTimer.isStarted()) {
			// shift gears
			fSharedOutputValues.setSolenoidOutputValue("so_gear_shifter", gearShiftButtonValue);
			fSharedInputValues.setBoolean("bi_is_low_gear", gearShiftButtonValue);
		}


	}


	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setSolenoidOutputValue("so_gear_shifter", false);
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