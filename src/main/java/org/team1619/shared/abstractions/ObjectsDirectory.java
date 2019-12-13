package org.team1619.shared.abstractions;

import org.team1619.models.behavior.Behavior;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.models.state.State;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

public interface ObjectsDirectory {
	//Inputs
	void registerAllInputs(YamlConfigParser booleanInputParser, YamlConfigParser numericInputParser, YamlConfigParser vectorInputParser);

	void registerBooleanInput(String name, Config config);

	void registerNumericInput(String name, Config config);

	void registerVectorInput(String name, Config config);

	 BooleanInput getBooleanInputObject(String name);

	 NumericInput getNumericInputObject(String name);

	 VectorInput getVectorInputObject(String name);

	//States
	void registerAllStates(YamlConfigParser parser);

	void registerStates(String name, YamlConfigParser statesParser, Config config);

	State getStateObject(String name);

	void setStateObject(String name, State state);

	//Outputs
	void registerAllOutputs(YamlConfigParser motorsParser, YamlConfigParser solenoidsParser);

	void registerMotor(String name, Config config, YamlConfigParser parser);

	void registerSolenoid(String name, Config config, YamlConfigParser parser);

	Motor getMotorObject(String motorName);

	Solenoid getSolenoidObject(String solenoidName);

	//Behaviors
	void setBehaviorObject(String name, Behavior behavior);

	Behavior getBehaviorObject(String name);

	//Motors
	void setCTREMotorObject(Integer deviceNumber, Object ctreMotor);

	Object getCTREMotorObject(Integer deviceNumber);

}
