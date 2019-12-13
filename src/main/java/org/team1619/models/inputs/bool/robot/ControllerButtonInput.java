package org.team1619.models.inputs.bool.robot;

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.bool.ButtonInput;
import org.team1619.utilities.Config;

public class ControllerButtonInput extends ButtonInput {
	private static final Logger sLogger = LogManager.getLogger(RobotJoystickButtonInput.class);

	private final XboxController fController;

	public ControllerButtonInput(Object name, Config config) {
		super(name, config);
		fController = new XboxController(fPort);
	}

	@Override
	public boolean isPressed() {
		switch (fButton) {
			case "a":
				return this.fController.getAButton();
			case "x":
				return this.fController.getXButton();
			case "y":
				return this.fController.getYButton();
			case "b":
				return this.fController.getBButton();
			case "start":
				return this.fController.getStartButton();
			case "back":
				return this.fController.getBackButton();
			case "left_bumper":
				return this.fController.getBumper(GenericHID.Hand.kLeft);
			case "right_bumper":
				return this.fController.getBumper(GenericHID.Hand.kRight);
			case "left_stick_button":
				return fController.getStickButton(GenericHID.Hand.kLeft);
			case "right_stick_button":
				return fController.getStickButton(GenericHID.Hand.kRight);
			case "d_pad_up":
				//getPOV(0) returns 0 when not plugged in
				if (fController.getPOVCount() != 0) {
					if (this.fController.getPOV(0) == 0) {
						return true;
					}
				}
				return false;
			case "d_pad_down":
				if (this.fController.getPOV(0) == 180) {
					return true;
				}
				return false;

			case "d_pad_left":
				if (this.fController.getPOV(0) == 270) {
					return true;
				}
				return false;

			case "d_pad_right":
				if (this.fController.getPOV(0) == 90) {
					return true;
				}
				return false;
			case "right_trigger":
				return fController.getRawAxis(3) > 0.5;
			case "left_trigger":
				return fController.getRawAxis(2) > 0.5;
			default:
				return false;
		}


	}
}
