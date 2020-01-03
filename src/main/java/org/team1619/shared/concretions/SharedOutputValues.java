package org.team1619.shared.concretions;

import org.team1619.models.outputs.motors.MotorGroup;
import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.OutputValues;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedOutputValues implements OutputValues {

	private static final Logger sLogger = LogManager.getLogger(SharedOutputValues.class);

	private Map<String, Map<String, Object>> fMotorOutputs = new ConcurrentHashMap<>();
	private Map<String, Boolean> fSolenoidOutputsValues = new ConcurrentHashMap<>();
	private Map<String, Map<Integer, Double>> fMotorCurrentValues = new ConcurrentHashMap<>();

	//Motor
	@Override
	public Map<String, Object> getMotorOutputs(String motorName) {
		return  fMotorOutputs.getOrDefault(motorName, Map.of("value", 0.0, "type", MotorGroup.OutputType.PERCENT));
	}

	@Override
	public Map<String, Object> getAllOutputs() {
		Map<String, Object> allOutputs = new HashMap<>();

		for (HashMap.Entry<String, Map<String,Object>> motor : fMotorOutputs.entrySet()) {
			allOutputs.put(motor.getKey(), motor.getValue().get("value"));
		}
		allOutputs.putAll(fSolenoidOutputsValues);

		return allOutputs;
	}

	@Override
	public void setMotorOutputValue(String motorName, Motor.OutputType motorType, double outputValue, @Nullable Object flag) {
		//sLogger.debug("Setting motor '{}' to {} ({})", motorName, outputValue, motorType);
		//Add the motor outputs to the fMotorOutputs map
		if(flag != null) {
			fMotorOutputs.put(motorName, Map.of("value", outputValue, "type", motorType, "flag", flag));
		} else {
			//If a null flag was passed in, do not add anything to the map so if the key 'flag' is called it will return null
			fMotorOutputs.put(motorName, Map.of("value", outputValue, "type", motorType));
		}
	}

	@Override
	public void putMotorCurrentValues(String motorName, Map<Integer, Double> motorCurrentValues) {
		fMotorCurrentValues.put(motorName, motorCurrentValues);
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues(String motorName) {
		return fMotorCurrentValues.get(motorName);
	}

	//Solenoid
	@Override
	public boolean getSolenoidOutputValue(String solenoidName) {
		return fSolenoidOutputsValues.getOrDefault(solenoidName, false);
	}

	@Override
	public void setSolenoidOutputValue(String solenoidName, boolean outputValue) {
		//sLogger.debug("Setting solenoid '{}' to {}", solenoidName, outputValue);
		fSolenoidOutputsValues.put(solenoidName, outputValue);
	}

}
