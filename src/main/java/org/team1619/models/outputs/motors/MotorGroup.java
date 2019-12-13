package org.team1619.models.outputs.motors;

import org.team1619.models.exceptions.ConfigurationInvalidTypeException;
import org.team1619.robot.ModelFactory;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Motor group holds a reference to a master CTRE motor, and all its follower CTRE motors.
 * A motor group acts just like a regular motor to the framework, but sets all the followers into FOLLOWER mode,
 * and passes all setHardware calls to the master motor.
 */

public class MotorGroup implements Motor {

	protected final Object fName;
	private final CTREMotor fMaster;
	private final Set<CTREMotor> fSlaves = new HashSet<>();

	public MotorGroup(Object name, Config config, YamlConfigParser parser, ModelFactory modelFactory) {
		fName = name;

		String master = config.getString("master");
		Motor motor = modelFactory.createMotor(master, parser.getConfig(master), parser);
		if (!(motor instanceof CTREMotor)) {
			throw new ConfigurationInvalidTypeException("Talon", "master", motor);
		}
		fMaster = (CTREMotor) motor;

		for (Object slaveName : config.getList("followers")) {
			motor = modelFactory.createMotor(slaveName, parser.getConfig(slaveName), parser);
			if (!(motor instanceof CTREMotor)) {
				throw new ConfigurationInvalidTypeException("Motor", "follower", motor);
			}

			CTREMotor slave = (CTREMotor) motor;
			slave.setHardware(OutputType.FOLLOWER, fMaster.getDeviceNumber(), null);
			fSlaves.add(slave);
		}
	}

	@Override
	public void setHardware(OutputType outputType, double outputValue, @Nullable Object flag) {
		fMaster.setHardware(outputType, outputValue, flag);
	}

	@Override
	public Map<Integer, Double> getMotorCurrentValues() {
		Map<Integer, Double> motorCurrentValues = fMaster.getMotorCurrentValues();
		for (CTREMotor motor : fSlaves) {
			motorCurrentValues.putAll(motor.getMotorCurrentValues());
		}
		return motorCurrentValues;
	}
}
