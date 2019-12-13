package org.team1619.robot;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.bool.sim.SimButtonInput;
import org.team1619.models.inputs.bool.sim.SimDigitalInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.numeric.sim.SimAnalogSensorInput;
import org.team1619.models.inputs.numeric.sim.SimAxisInput;
import org.team1619.models.inputs.vector.OdometryInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.models.inputs.vector.sim.SimAccelerationInput;
import org.team1619.models.inputs.vector.sim.SimLimelight;
import org.team1619.models.inputs.vector.sim.SimNavxInput;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.motors.MotorGroup;
import org.team1619.models.outputs.motors.sim.SimRumble;
import org.team1619.models.outputs.motors.sim.SimServo;
import org.team1619.models.outputs.motors.sim.SimTalon;
import org.team1619.models.outputs.motors.sim.SimVictor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.models.outputs.solenoids.sim.SimSolenoidDouble;
import org.team1619.models.outputs.solenoids.sim.SimSolenoidSingle;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

public abstract class SimModelFactory extends ModelFactory {

	private static final Logger sLogger = LogManager.getLogger(SimModelFactory.class);

	private final EventBus fEventBus;

	@Inject
	public SimModelFactory(EventBus eventBus, InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		super(inputValues, outputValues, dashboard, robotConfiguration, objectsDirectory);
		fEventBus = eventBus;
	}

	@Override
	public Motor createMotor(Object name, Config config, YamlConfigParser parser) {
		sLogger.debug("Creating motor '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "talon":
				return new SimTalon(name, config, fEventBus, fSharedObjectDirectory, fSharedInputValues);
			case "victor":
				return new SimVictor(name, config, fSharedObjectDirectory);
			case "motor_group":
				return new MotorGroup(name, config, parser, this);
			case "servo":
				return new SimServo(name, config);
			case "rumble":
				return new SimRumble(name, config);
			default:
				return super.createMotor(name, config, parser);
		}
	}

	@Override
	public Solenoid createSolenoid(Object name, Config config, YamlConfigParser parser) {
		sLogger.debug("Creating solenoid '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());
		switch (config.getType()) {
			case "solenoid_single":
				return new SimSolenoidSingle(name, config);
			case "solenoid_double":
				return new SimSolenoidDouble(name, config);
			default:
				return super.createSolenoid(name, config, parser);
		}
	}

	@Override
	public BooleanInput createBooleanInput(Object name, Config config) {
		sLogger.debug("Creating boolean input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "joystick_button":
				return new SimButtonInput(fEventBus, name, config);
			case "controller_button":
				return new SimButtonInput(fEventBus, name, config);
			case "digital_input":
				return new SimDigitalInput(fEventBus, name, config);
			default:
				return super.createBooleanInput(name, config);
		}
	}

	@Override
	public NumericInput createNumericInput(Object name, Config config) {
		sLogger.debug("Creating numeric input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "joystick_axis":
				return new SimAxisInput(fEventBus, name, config);
			case "controller_axis":
				return new SimAxisInput(fEventBus, name, config);
			case "analog_sensor":
				return new SimAnalogSensorInput(fEventBus, name, config);
			default:
				return super.createNumericInput(name, config);
		}
	}

	@Override
	public VectorInput createVectorInput(Object name, Config config) {
		sLogger.debug("Creating vector input '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (config.getType()) {
			case "accelerometer_input":
				return new SimAccelerationInput(fEventBus, name, config, fSharedInputValues);
			case "odometry_input":
				return new OdometryInput(name, config, fSharedInputValues);
			case "limelight":
				return new SimLimelight(fEventBus, name, config);
			case "navx":
				return new SimNavxInput(fEventBus, name, config);
			default:
				return super.createVectorInput(name, config);
		}
	}

}