package org.team1619.models.outputs.motors;

/**
 * CTREMotor is a class that stores data specific to CTRE motors, TalonSRX and VictorSPX
 */

public abstract class CTREMotor implements Motor, DeviceNumberedMotor {

	protected final int fDeviceNumber;
	protected final Object fName;
	protected final boolean fIsBrakeModeEnabled;
	protected final boolean fIsInverted;

	public CTREMotor(int deviceNumber, Object name, boolean isBrakeModeEnabled, boolean isInverted) {
		fDeviceNumber = deviceNumber;
		fName = name;
		fIsBrakeModeEnabled = isBrakeModeEnabled;
		fIsInverted = isInverted;
	}

	@Override
	public int getDeviceNumber() {
		return fDeviceNumber;
	}
}
