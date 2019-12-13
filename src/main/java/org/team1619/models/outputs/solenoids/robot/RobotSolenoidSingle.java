package org.team1619.models.outputs.solenoids.robot;

import org.team1619.models.outputs.solenoids.SolenoidSingle;
import org.team1619.utilities.Config;


public class RobotSolenoidSingle extends SolenoidSingle {

	private final edu.wpi.first.wpilibj.Solenoid fWpiSolenoid;

	public RobotSolenoidSingle(Object name, Config config) {
		super(name, config);
		fWpiSolenoid = new edu.wpi.first.wpilibj.Solenoid(fDeviceNumber);
	}


	@Override
	public void setHardware(boolean output) {
		fWpiSolenoid.set(output);
	}

}