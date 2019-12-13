package org.team1619.models.outputs.solenoids.robot;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import org.team1619.models.outputs.solenoids.SolenoidDouble;
import org.team1619.utilities.Config;


public class RobotSolenoidDouble extends SolenoidDouble {

	private final edu.wpi.first.wpilibj.DoubleSolenoid fWpiSolenoid;

	public RobotSolenoidDouble(Object name, Config config) {
		super(name, config);
		fWpiSolenoid = new edu.wpi.first.wpilibj.DoubleSolenoid(fDeviceNumberMaster, fDeviceNumberSlave);
		fWpiSolenoid.set(DoubleSolenoid.Value.kOff);
	}


	@Override
	public void setHardware(boolean output) {
		if (output) {
			fWpiSolenoid.set(DoubleSolenoid.Value.kForward);
		} else {
			fWpiSolenoid.set(DoubleSolenoid.Value.kReverse);
		}
	}

}