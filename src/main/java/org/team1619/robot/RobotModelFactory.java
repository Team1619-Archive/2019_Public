package org.team1619.robot;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.bool.robot.ControllerButtonInput;
import org.team1619.models.inputs.bool.robot.RobotDigitalInput;
import org.team1619.models.inputs.bool.robot.RobotJoystickButtonInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.numeric.robot.*;
import org.team1619.models.inputs.vector.OdometryInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.models.inputs.vector.robot.RobotAccelerationInput;
import org.team1619.models.inputs.vector.robot.RobotLimelight;
import org.team1619.models.inputs.vector.robot.RobotNavxInput;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.motors.MotorGroup;
import org.team1619.models.outputs.motors.robot.RobotRumble;
import org.team1619.models.outputs.motors.robot.RobotServo;
import org.team1619.models.outputs.motors.robot.RobotTalon;
import org.team1619.models.outputs.motors.robot.RobotVictor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.models.outputs.solenoids.robot.RobotSolenoidDouble;
import org.team1619.models.outputs.solenoids.robot.RobotSolenoidSingle;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

public abstract class RobotModelFactory extends ModelFactory {

	private static final Logger sLogger = LogManager.getLogger(RobotModelFactory.class);

	@Inject
	public RobotModelFactory(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		super(inputValues, outputValues, dashboard, robotConfiguration, objectsDirectory);
	}

	@Override
	public Motor createMotor(Object name, Config config, YamlConfigParser parser) {
		sLogger.debug("Creating motor '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "talon":
				return new RobotTalon(name, config, fSharedObjectDirectory, fSharedInputValues);
			case "victor":
				return new RobotVictor(name, config, fSharedObjectDirectory);
			case "motor_group":
				return new MotorGroup(name, config, parser, this);
			case "servo":
				return new RobotServo(name, config);
			case "rumble":
				return new RobotRumble(name, config);
			default:
				return super.createMotor(name, config, parser);
		}
	}

	@Override
	public Solenoid createSolenoid(Object name, Config config, YamlConfigParser parser) {
		sLogger.debug("Creating solenoid '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "solenoid_single":
				return new RobotSolenoidSingle(name, config);
			case "solenoid_double":
				return new RobotSolenoidDouble(name, config);
			default:
				return super.createSolenoid(name, config, parser);
		}
	}

	@Override
	public BooleanInput createBooleanInput(Object name, Config config) {
		sLogger.debug("Creating boolean input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "joystick_button":
				return new RobotJoystickButtonInput(name, config);
			case "controller_button":
				return new ControllerButtonInput(name, config);
			case "digital_input":
				return new RobotDigitalInput(name, config);
			default:
				return super.createBooleanInput(name, config);
		}
	}

	@Override
	public NumericInput createNumericInput(Object name, Config config) {
		sLogger.debug("Creating numeric input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "joystick_axis":
				return new RobotJoystickAxisInput(name, config);
			case "controller_axis":
				return new RobotControllerAxisInput(name, config);
			case "analog_sensor":
				return new RobotAnalogSensorInput(name, config);
			default:
				return super.createNumericInput(name, config);
		}
	}

	@Override
	public VectorInput createVectorInput(Object name, Config config) {
		sLogger.debug("Creating vector input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "accelerometer_input":
				return new RobotAccelerationInput(name, config, fSharedInputValues);
			case "odometry_input":
				return new OdometryInput(name, config, fSharedInputValues);
			case "limelight":
				return new RobotLimelight(name, config);
			case "navx":
				return new RobotNavxInput(name, config);
			default:
				return super.createVectorInput(name, config);
		}
	}

}