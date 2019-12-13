package org.team1619.models.outputs.motors.robot;

import com.ctre.phoenix.motorcontrol.*;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import org.team1619.models.outputs.motors.Talon;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.utilities.Config;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * RobotTalon extends Talon, and controls talon motor controllers on the robot
 */

public class RobotTalon extends Talon {

	private static final Logger sLogger = LogManager.getLogger(RobotTalon.class);

	private static final int CAN_TIMEOUT_MILLISECONDS = 10;

	private final TalonSRX fMotor;
	private final double fMaxCountsPer100ms;
	private double fMaxCurrent;
	private ObjectsDirectory fSharedObjectsDirectory;

	private final Map<String, Map<String, Double>> fProfiles;

	private String fCurrentProfileName = "none";

	public RobotTalon(Object name, Config config, ObjectsDirectory objectsDirectory, InputValues inputValues) {
		super(name, config, inputValues);

		fSharedObjectsDirectory = objectsDirectory;
		// Only create one WPILib TalonSRX class per physical talon (device number). Otherwise, the physical talon gets confused getting instructions from multiple TalonSRX classes
		Object motorObject = fSharedObjectsDirectory.getCTREMotorObject(fDeviceNumber);
		//noinspection ConstantConditions
		if (motorObject == null) {
			fMotor = new TalonSRX(fDeviceNumber);
			fSharedObjectsDirectory.setCTREMotorObject(fDeviceNumber, fMotor);
		} else {
			fMotor = (TalonSRX) motorObject;
		}

		fMotor.configFactoryDefault(CAN_TIMEOUT_MILLISECONDS);

		if (!(config.get("profiles", new HashMap<>()) instanceof Map)) throw new RuntimeException();
		fProfiles = (Map<String, Map<String, Double>>) config.get("profiles", new HashMap<>());

		fMaxCountsPer100ms = config.getDouble("max_counts_per_100ms", 0);

		fMotor.setInverted(fIsInverted);
		fMotor.setNeutralMode(fIsBrakeModeEnabled ? NeutralMode.Brake : NeutralMode.Coast);

		fMotor.enableCurrentLimit(fCurrentLimitEnabled);
		fMotor.configContinuousCurrentLimit(fContinuousCurrentLimitAmps, CAN_TIMEOUT_MILLISECONDS);
		fMotor.configPeakCurrentLimit(fPeakCurrentLimitAmps, CAN_TIMEOUT_MILLISECONDS);
		fMotor.configPeakCurrentDuration(fPeakCurrentDurationMilliseconds, CAN_TIMEOUT_MILLISECONDS);

		fMotor.setSensorPhase(fSensorInverted);

		switch (fFeedbackDevice) {
			case "quad_encoder":
				fMotor.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 0, CAN_TIMEOUT_MILLISECONDS);

				/*TalonSRXPIDSetConfiguration talonSRXPIDSetConfiguration = new TalonSRXPIDSetConfiguration();
				talonSRXPIDSetConfiguration.selectedFeedbackCoefficient = 3.0;
				fMotor.configurePID(talonSRXPIDSetConfiguration);*/
				break;
			default:

				break;
		}

		fMaxCurrent = 0.0;
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		readEncoderValues();
		if (flag != null && flag.equals("zero")) {
			zeroSensor();
			flag = null;
		}


		switch (outputType) {
			case PERCENT:

				fMotor.set(ControlMode.PercentOutput, outputValue);
				break;
			case FOLLOWER:
				fMotor.follow((IMotorController) fSharedObjectsDirectory.getCTREMotorObject((int) outputValue), FollowerType.PercentOutput);
				break;
			case VELOCITY_CONTROLLED:

				if (flag == null) {
					throw new RuntimeException("PIDF Profile name must be a String");
				}

				String velocityProfileName = String.valueOf(flag);

				if (!velocityProfileName.equals(fCurrentProfileName)) {
					setProfile(velocityProfileName);
				}

				fMotor.set(ControlMode.Velocity, outputValue * fMaxCountsPer100ms);
				break;
			case POSITION_CONTROLLED:
				throw new RuntimeException("POSITION_CONTROLLED mode for talon is broken");

				/*if (flag == null) {
					throw new RuntimeException("PIDF Profile name must be a String");
				}

				String positionProfileName = String.valueOf(flag);

				if(!positionProfileName.equals(fCurrentProfileName)) {
					setProfile(positionProfileName);
				}

				fMotor.set(ControlMode.Position, outputValue * fCountsPerUnit);
				break;*/
			case MOTION_MAGIC:
				if (!(flag instanceof String)) {
					throw new RuntimeException("PIDF Profile name must be a String");
				}

				String motionMagicProfileName = String.valueOf(flag);

				if (!motionMagicProfileName.equals(fCurrentProfileName)) {
					setProfile(motionMagicProfileName);
				}

				fMotor.set(ControlMode.MotionMagic, outputValue * fCountsPerUnit);
				break;
			default:
				throw new RuntimeException("No output type " + outputType + " for TalonSRX");
		}
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		double motorCurrent = fMotor.getOutputCurrent();
		if (motorCurrent > fMaxCurrent) {
			fMaxCurrent = motorCurrent;
		}
		motorCurrentValues.put(fDeviceNumber, motorCurrent);
		return motorCurrentValues;
	}

	@Override
	public double getSensorPosition() {
		return fMotor.getSelectedSensorPosition(0) / fCountsPerUnit;
	}

	@Override
	public double getSensorVelocity() {
		return fMotor.getSelectedSensorVelocity(0) / fCountsPerUnit * 10.0;
	}

	@Override
	public void zeroSensor() {
		switch (fFeedbackDevice) {
			case "quad_encoder":
				fMotor.getSensorCollection().setQuadraturePosition(0, CAN_TIMEOUT_MILLISECONDS);
				break;
			default:
				break;
		}
	}

	private void setProfile(String profileName) {
		if (!fProfiles.containsKey(profileName)) {
			throw new RuntimeException("PIDF Profile " + profileName + " doesn't exist");
		}

		Config profile = new Config("pidf_config", fProfiles.get(profileName));
		fMotor.config_kP(0, profile.getDouble("p", 0.0), CAN_TIMEOUT_MILLISECONDS);
		fMotor.config_kI(0, profile.getDouble("i", 0.0), CAN_TIMEOUT_MILLISECONDS);
		fMotor.config_kD(0, profile.getDouble("d", 0.0), CAN_TIMEOUT_MILLISECONDS);
		fMotor.config_kF(0, profile.getDouble("f", 0.0), CAN_TIMEOUT_MILLISECONDS);
		fMotor.configMotionAcceleration(profile.getInt("acceleration", 0), CAN_TIMEOUT_MILLISECONDS);
		fMotor.configMotionCruiseVelocity(profile.getInt("cruise_velocity", 0), CAN_TIMEOUT_MILLISECONDS);

		fCurrentProfileName = profileName;
	}
}
