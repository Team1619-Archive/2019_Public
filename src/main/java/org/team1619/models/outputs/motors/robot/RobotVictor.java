package org.team1619.models.outputs.motors.robot;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FollowerType;
import com.ctre.phoenix.motorcontrol.IMotorController;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.motorcontrol.can.VictorSPX;
import org.team1619.models.outputs.motors.Victor;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * RobotVictor extends Victor, and controls victor motor controllers on the robot
 */

public class RobotVictor extends Victor {

	private final VictorSPX fMotor;
	private double fMaxCurrent;
	private ObjectsDirectory fSharedObjectsDirectory;

	private OutputType fLastOutputType = OutputType.ZERO;
	private double fLastOutputValue = 0.0;
	@Nullable
	private Object fLastFlag = null;

	public RobotVictor(Object name, Config config, ObjectsDirectory objectsDirectory) {
		super(name, config);
		fSharedObjectsDirectory = objectsDirectory;
		// Only create one WPILib VictorSPX class per physical victor (device number). Otherwise, the physical victor gets confused getting instructions from multiple VictorSPX classes
		Object motorObject = fSharedObjectsDirectory.getCTREMotorObject(fDeviceNumber);
		//noinspection ConstantConditions
		if(motorObject == null){
			fMotor = new VictorSPX(fDeviceNumber);
			fSharedObjectsDirectory.setCTREMotorObject(fDeviceNumber, fMotor);
		} else {
			fMotor = (VictorSPX) motorObject;
		}

		fMotor.setInverted(fIsInverted);
		fMotor.setNeutralMode(fIsBrakeModeEnabled ? NeutralMode.Brake : NeutralMode.Coast);

		fMaxCurrent = 0.0;
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		switch (outputType) {
			case PERCENT:
				fMotor.set(ControlMode.PercentOutput, outputValue);
				break;
			case FOLLOWER:
				fMotor.follow((IMotorController) fSharedObjectsDirectory.getCTREMotorObject((int) outputValue), FollowerType.PercentOutput);
				break;
			default:
				throw new RuntimeException("No output type " + outputType + " for VictorSPX");
		}
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		motorCurrentValues.put(fDeviceNumber, 0.0);
		return motorCurrentValues;
	}
}
