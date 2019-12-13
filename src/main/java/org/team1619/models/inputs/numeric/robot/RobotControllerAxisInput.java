package org.team1619.models.inputs.numeric.robot;

import edu.wpi.first.wpilibj.XboxController;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.numeric.AxisInput;
import org.team1619.utilities.Config;

public class RobotControllerAxisInput extends AxisInput {

	private static final Logger sLogger = LogManager.getLogger(RobotJoystickAxisInput.class);

	private XboxController fController;

	public RobotControllerAxisInput(Object name, Config config) {
		super(name, config);
		fController = new XboxController(fPort);
	}

	@Override
	public double getAxis() {
		return fController.getRawAxis(fAxis);
	}
}
