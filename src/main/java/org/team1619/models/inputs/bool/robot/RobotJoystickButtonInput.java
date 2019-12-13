package org.team1619.models.inputs.bool.robot;

import edu.wpi.first.wpilibj.Joystick;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.bool.ButtonInput;
import org.team1619.utilities.Config;

public class RobotJoystickButtonInput extends ButtonInput {

	private static final Logger sLogger = LogManager.getLogger(RobotJoystickButtonInput.class);

	private final Joystick fJoystick;

	public RobotJoystickButtonInput(Object name, Config config) {
		super(name, config);
		fJoystick = new Joystick(fPort);
	}

	@Override
	public boolean isPressed() {
		return fJoystick.getRawButton(Integer.valueOf(fButton));
	}
}
