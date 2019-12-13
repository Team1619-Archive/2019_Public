package org.team1619.robot;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.models.exceptions.ConfigurationTypeDoesNotExistException;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.models.state.*;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

/**
 * Handles the creation of states
 */

public abstract class ModelFactory {
	private static final Logger sLogger = LogManager.getLogger(ModelFactory.class);

	protected final InputValues fSharedInputValues;
	protected final OutputValues fSharedOutputValues;
	protected final Dashboard fDashboard;
	protected final RobotConfiguration fRobotConfiguration;
	protected final ObjectsDirectory fSharedObjectDirectory;

	@Inject
	public ModelFactory(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fRobotConfiguration = robotConfiguration;
		fSharedObjectDirectory = objectsDirectory;
	}

	public Motor createMotor(Object name, Config config, YamlConfigParser parser) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public Solenoid createSolenoid(Object name, Config config, YamlConfigParser parser) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public BooleanInput createBooleanInput(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public NumericInput createNumericInput(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public VectorInput createVectorInput(Object name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public Behavior createBehavior(String name, Config config) {
		throw new ConfigurationTypeDoesNotExistException(config.getType());
	}

	public State createState(String name, YamlConfigParser parser, Config config) {
		sLogger.debug("Creating state '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		//todo - is there a better way to handle nullable

		//Only create one instance of each state
		State state = fSharedObjectDirectory.getStateObject(name);
		//noinspection ConstantConditions
		if(state == null) {
			switch (config.getType()) {
				case "single_state":
					state = new SingleState(this, name, config, fSharedObjectDirectory);
					break;
				case "parallel_state":
					state =  new ParallelState(this, name, parser, config);
					break;
				case "sequencer_state":
					state =  new SequencerState(this, name, parser, config);
					break;
				case "timed_state":
					state =  new TimedState(this, name, parser, config);
					break;
				case "done_for_time_state":
					state =  new DoneForTimeState(this, name, parser, config);
					break;
				default:
					throw new ConfigurationException("State of name " + name + " does not exist.");
			}
			fSharedObjectDirectory.setStateObject(name, state);
		}
		return state;
	}

}
