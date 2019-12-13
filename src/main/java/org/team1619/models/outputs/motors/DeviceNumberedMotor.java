package org.team1619.models.outputs.motors;

/**
 * DeviceNumberedMotor has a single method getDeviceNumber,
 * and is extended by all motors that have a device number.
 */

public interface DeviceNumberedMotor extends Motor {

	int getDeviceNumber();
}
