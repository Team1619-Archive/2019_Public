package org.team1619.models.outputs.motors.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;

import org.team1619.models.outputs.motors.Victor;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * SimVictor extends Victor, and acts like victors in sim mode
 */

public class SimVictor extends Victor {

	private static final Logger sLogger = LogManager.getLogger(SimVictor.class);

	private double fOutput = 0.0;
	private Integer fMotor;

	public SimVictor(Object name, Config config, ObjectsDirectory objectsDirectory) {
		super(name, config);

		// Included to mimic RobotTalon for testing
		fMotor = (Integer)objectsDirectory.getCTREMotorObject(fDeviceNumber);
		//noinspection ConstantConditions
		if(fMotor == null){
			Integer deviceNumber = fDeviceNumber;
			fMotor = deviceNumber;
			objectsDirectory.setCTREMotorObject(fDeviceNumber, fMotor);
		}
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		fOutput = outputValue;
//		sLogger.trace("{}", outputValue);
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		double motorCurrent = fDeviceNumber + 100.0;
		motorCurrentValues.put(fDeviceNumber, motorCurrent);
		//sLogger.debug("Motor ID {} has Current Value of {}", fDeviceNumber, motorCurrent);
		return motorCurrentValues;
	}
}
