package org.team1619.models.outputs.motors.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import org.team1619.models.outputs.motors.Rumble;
import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * RobotRumble extends Rumble, and controls xbox controller rumble motors when running on the robot
 */

public class RobotRumble extends Rumble {

	private final XboxController fRumble;
	private double fAdjustedOutput;

	public RobotRumble(Object name, Config config) {
		super(name, config);
		fRumble = new XboxController(fPort);
		fAdjustedOutput = 0.0;
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		fAdjustedOutput = outputValue;
		if (fRumbleSide.equals("right")) {
			fRumble.setRumble(GenericHID.RumbleType.kRightRumble, fAdjustedOutput);
		} else if (fRumbleSide.equals("left")) {
			fRumble.setRumble(GenericHID.RumbleType.kLeftRumble, fAdjustedOutput);
		}
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = new HashMap<>();
		motorCurrentValues.put(fPort, fAdjustedOutput);
		return motorCurrentValues;
	}
}
