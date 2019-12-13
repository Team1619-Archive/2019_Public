package org.team1619.models.outputs.motors;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Motor holds the OutputType enum specifying all available motor output types,
 * and methods needed by all motors.
 * The setHardware is how all motor types of motors are set by the framework,
 * which allows all motors to be treated the same by the output service.
 */

public interface Motor {

	enum OutputType {
		PERCENT,
		POSITION_CONTROLLED,
		VELOCITY_CONTROLLED,
		MOTION_MAGIC,
		FOLLOWER,
		SERVO,
		RUMBLE,
		ZERO
	}

	void setHardware(OutputType outputType, double outputValue, @Nullable Object flag);

	Map<Integer, Double> getMotorCurrentValues();
}
