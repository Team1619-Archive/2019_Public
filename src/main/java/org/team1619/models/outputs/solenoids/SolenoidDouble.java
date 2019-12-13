package org.team1619.models.outputs.solenoids;

import org.team1619.utilities.Config;

public abstract class SolenoidDouble implements Solenoid {

	protected final Object fName;
	protected final int fDeviceNumberMaster;
	protected final int fDeviceNumberSlave;

	public SolenoidDouble(Object name, Config config) {
		fName = name;
		fDeviceNumberMaster = config.getInt("device_number_master");
		fDeviceNumberSlave = config.getInt("device_number_slave");
	}

//	public int getDeviceNumber() {
//		return fDeviceNumberMaster;
//	}

}
