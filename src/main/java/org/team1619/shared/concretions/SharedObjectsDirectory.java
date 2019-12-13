package org.team1619.shared.concretions;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.models.state.State;
import org.team1619.robot.ModelFactory;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;
import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.injection.Inject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles the creation and use of all objects (inputs, states, outputs, behaviors, talons)
 */

@Singleton
public class SharedObjectsDirectory implements ObjectsDirectory {

	private static final Logger sLogger = LogManager.getLogger(SharedObjectsDirectory.class);

	private final ModelFactory fModelFactory;
	private final RobotConfiguration fRobotConfiguration;
	private final InputValues fSharedInputValues;
	private final Map<String, BooleanInput> fBooleanInputObjects = new ConcurrentHashMap<>();
	private final Map<String, NumericInput> fNumericInputObjects = new ConcurrentHashMap<>();
	private final Map<String, VectorInput> fVectorInputObjects = new ConcurrentHashMap<>();
	private final Map<String, Motor> fMotorObjects = new ConcurrentHashMap<>();
	private final Map<String, Solenoid> fSolenoidObjects = new ConcurrentHashMap<>();
	private final Map<Object, State> fStateObjects = new ConcurrentHashMap<>();
	private final Map<Object, Behavior> fBehaviorObjects = new ConcurrentHashMap<>();
	private final Map<Integer, Object> fCTREMotorObjects = new ConcurrentHashMap<>();

	@Inject
	public SharedObjectsDirectory(ModelFactory modelFactory, RobotConfiguration robotConfiguration, InputValues inputValues) {
		fModelFactory = modelFactory;
		fRobotConfiguration = robotConfiguration;
		fSharedInputValues = inputValues;
	}

	//--------------------------- Inputs ----------------------------------------//

	@Override
	public void registerAllInputs(YamlConfigParser booleanInputParser, YamlConfigParser numericInputParser, YamlConfigParser vectorInputParser) {
		for (String booleanInputName : fRobotConfiguration.getBooleanInputNames()) {
			Config config = booleanInputParser.getConfig(booleanInputName);
			registerBooleanInput(booleanInputName, config);
			sLogger.debug("Registered {} in BooleanInputs", booleanInputName);
		}

		for (String numericInputName : fRobotConfiguration.getNumericInputNames()) {
			Config config = numericInputParser.getConfig(numericInputName);
			registerNumericInput(numericInputName, config);
			sLogger.debug("Registered {} in NumericInputs", numericInputName);
		}

		for (String vectorInputName : fRobotConfiguration.getVectorInputNames()) {
			Config config = vectorInputParser.getConfig(vectorInputName);
			registerVectorInput(vectorInputName, config);
			sLogger.debug("Registered {} in VectorInputs", vectorInputName);
		}

		for (BooleanInput booleanInput : fBooleanInputObjects.values()) {
			booleanInput.initialize();
		}
		sLogger.debug("Boolean inputs initialized");

		for (NumericInput numericInput : fNumericInputObjects.values()) {
			numericInput.initialize();
		}
		sLogger.debug("Numeric inputs initialized");

		for (VectorInput vectorInput : fVectorInputObjects.values()) {
			vectorInput.initialize();
		}
		sLogger.debug("Vector inputs initialized");

	}

	@Override
	public void registerBooleanInput(String name, Config config) {
		fBooleanInputObjects.put(name, fModelFactory.createBooleanInput(name, config));
	}

	@Override
	public void registerNumericInput(String name, Config config) {
		fNumericInputObjects.put(name, fModelFactory.createNumericInput(name, config));
	}

	@Override
	public void registerVectorInput(String name, Config config) {
		fVectorInputObjects.put(name, fModelFactory.createVectorInput(name, config));
	}

	@Override
	public BooleanInput getBooleanInputObject(String name) {
		return fBooleanInputObjects.get(name);
	}

	@Override
	public NumericInput getNumericInputObject(String name) {
		return fNumericInputObjects.get(name);
	}

	@Override
	public VectorInput getVectorInputObject(String name) {
		return fVectorInputObjects.get(name);
	}

	//--------------------------- States ----------------------------------------//
	@Override
	public void registerAllStates(YamlConfigParser statesParser) {
		for (String stateName : fRobotConfiguration.getStateNames()) {
			Config config = statesParser.getConfig(stateName);
			registerStates(stateName, statesParser, config);
		}
	}

	@Override
	public void registerStates(String name, YamlConfigParser statesParser, Config config) {
		fModelFactory.createState(name, statesParser, config);
	}

	@Override
	public State getStateObject(String name) {
		return fStateObjects.get(name);
	}

	@Override
	public void setStateObject(String name, State state) {
		fStateObjects.put(name, state);
	}

	//--------------------------- Outputs ----------------------------------------//
	@Override
	public void registerAllOutputs(YamlConfigParser motorsParser, YamlConfigParser solenoidsParser) {
		for (String motorName : fRobotConfiguration.getMotorNames()) {
			Config motorConfig = motorsParser.getConfig(motorName);
			registerMotor(motorName, motorConfig, motorsParser);
			sLogger.debug("Registered {} in Motors", motorName);
		}

		for (String solenoidName : fRobotConfiguration.getSolenoidNames()) {
			Config solenoidConfig = solenoidsParser.getConfig(solenoidName);
			registerSolenoid(solenoidName, solenoidConfig, solenoidsParser);
			sLogger.debug("Registered {} in Solenoids", solenoidName);
		}
	}

	@Override
	public void registerMotor(String name, Config config, YamlConfigParser parser) {
		fMotorObjects.put(name, fModelFactory.createMotor(name, config, parser));
	}

	@Override
	public void registerSolenoid(String name, Config config, YamlConfigParser parser) {
		fSolenoidObjects.put(name, fModelFactory.createSolenoid(name, config, parser));
	}

	@Override
	public Motor getMotorObject(String motorName) {
		return fMotorObjects.get(motorName);
	}

	@Override
	public Solenoid getSolenoidObject(String solenoidName) {
		return fSolenoidObjects.get(solenoidName);
	}

	//--------------------------- Behaviors ----------------------------------------//

	@Override
	public void setBehaviorObject(String name, Behavior behavior) {
		fBehaviorObjects.put(name, behavior);
	}

	@Override
	public Behavior getBehaviorObject(String name) {
		return fBehaviorObjects.get(name);
	}


	//--------------------------- Talons ----------------------------------------//

	@Override
	public void setCTREMotorObject(Integer deviceNumber, Object ctreMotor) {
		fCTREMotorObjects.put(deviceNumber, ctreMotor);
	}

	@Override
	public Object getCTREMotorObject(Integer deviceNumber) {
		return fCTREMotorObjects.getOrDefault(deviceNumber, null);
	}


}
