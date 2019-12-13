package org.team1619.models.inputs.bool.robot;

import org.team1619.models.inputs.bool.DigitalInput;
import org.team1619.utilities.Config;

public class RobotDigitalInput extends DigitalInput {
	private edu.wpi.first.wpilibj.DigitalInput fSensor;

	public RobotDigitalInput(Object name, Config config) {
		super(name, config);

		fSensor = new edu.wpi.first.wpilibj.DigitalInput(fId);
	}


	@Override
	public boolean getDigitalInputValue() {
		return fSensor.get();
	}
}
