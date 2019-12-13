package org.team1619.models.outputs.motors.sim;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.events.sim.SimNumericInputSetEvent;
import org.team1619.models.inputs.numeric.sim.SimNumericInputListener;
import org.team1619.models.outputs.motors.Talon;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * SimTalon extends Talon, and acts like talons in sim mode
 */

public class SimTalon extends Talon {

	private final SimNumericInputListener fPositionListener;
	private final SimNumericInputListener fVelocityListener;

	private static final Logger sLogger = LogManager.getLogger(SimTalon.class);

	private double fOutput = 0.0;
	private Integer fMotor;

	private final Map<String, Map<String, Double>> fProfiles;

	private String fCurrentProfileName = "none";

	public SimTalon(Object name, Config config, EventBus eventBus, ObjectsDirectory objectsDirectory, InputValues inputValues) {
		super(name, config, inputValues);

		fPositionListener = new SimNumericInputListener(eventBus, fPositionInputName);
		fVelocityListener = new SimNumericInputListener(eventBus, fVelocityInputName);

		// Included to mimic RobotTalon for testing
		fMotor = (Integer)objectsDirectory.getCTREMotorObject(fDeviceNumber);
		//noinspection ConstantConditions
		if (fMotor == null) {
			Integer deviceNumber = fDeviceNumber;
			fMotor = deviceNumber;
			objectsDirectory.setCTREMotorObject(fDeviceNumber, fMotor);
		}

		if(!(config.get("profiles", new HashMap<>()) instanceof Map)) throw new RuntimeException();
		fProfiles = (Map<String, Map<String, Double>>) config.get("profiles", new HashMap<>());
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		readEncoderValues();
		if(flag != null && flag.equals("zero")) {
			zeroSensor();
			flag = null;
		}

		switch (outputType) {
			case PERCENT:
				fOutput = outputValue;
				break;
			case FOLLOWER:
				fOutput = outputValue;
				break;
			case VELOCITY_CONTROLLED:
				if(!(flag instanceof String)) {
					throw new RuntimeException("PIDF Profile name must be a String");
				}

				String profileName = String.valueOf(flag);

				if(!profileName.equals(fCurrentProfileName)) {
					if(!fProfiles.containsKey(profileName)) {
						throw new RuntimeException("PIDF Profile " + profileName + " doesn't exist");
					}

					fCurrentProfileName = profileName;
				}

				fOutput = outputValue;

				break;
			case POSITION_CONTROLLED:
				fOutput = outputValue;
				break;
			case MOTION_MAGIC:
				fOutput = outputValue;
				break;
			default:
				throw new RuntimeException("No output type " + outputType + " for TalonSRX");
		}
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

	@Override
	public double getSensorPosition() {
		return fPositionListener.get();
	}

	@Override
	public double getSensorVelocity() {
		return fVelocityListener.get();
	}

	@Override
	public void zeroSensor() {
		fPositionListener.onNumericInputSet(new SimNumericInputSetEvent(fPositionInputName, 0));
	}
}
