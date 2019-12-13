package org.team1619.shared.concretions;

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

	private Map<String, Double> fMotorOutputsValues = new ConcurrentHashMap<>();
	private Map<String, Motor.OutputType> fMotorOutputTypes = new ConcurrentHashMap<>();
	private Map<String, Object> fMotorFlags = new ConcurrentHashMap<>();
	private Map<String, Boolean> fSolenoidOutputsValues = new ConcurrentHashMap<>();
	private Map<String, Map<Integer, Double>> fMotorCurrentValues = new ConcurrentHashMap<>();

	//Motor
	@Override
	public double getMotorOutputValue(String motorName) {
		return fMotorOutputsValues.getOrDefault(motorName, 0.0);
	}

	@Override
	public Map<String, Object> getAllOutputs() {
		Map<String, Object> allOutputs = new HashMap<>();

		allOutputs.putAll(fMotorOutputsValues);
		allOutputs.putAll(fSolenoidOutputsValues);

		return allOutputs;
	}

	@Override
	public Motor.OutputType getMotorType(String motorName) {
		return fMotorOutputTypes.getOrDefault(motorName, Motor.OutputType.PERCENT);
	}

	@Override
	public Object getMotorFlag(String motorName) {
		return fMotorFlags.getOrDefault(motorName, null);
	}

	@Override
	public void setMotorOutputValue(String motorName, Motor.OutputType motorType, double outputValue, @Nullable Object flag) {
		//sLogger.debug("Setting motor '{}' to {} ({})", motorName, outputValue, motorType);
		fMotorOutputsValues.put(motorName, outputValue);
		fMotorOutputTypes.put(motorName, motorType);
		if(flag == null){
			fMotorFlags.remove(motorName);
		} else {
			fMotorFlags.put(motorName, flag);
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
