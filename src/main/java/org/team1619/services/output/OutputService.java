package org.team1619.services.output;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.models.outputs.solenoids.Solenoid;
import org.team1619.robot.ModelFactory;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.YamlConfigParser;

import java.util.concurrent.TimeUnit;

public class OutputService extends AbstractScheduledService {

	private static final Logger sLogger = LogManager.getLogger(OutputService.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final ObjectsDirectory fSharedOutputsDirectory;
	private final RobotConfiguration fRobotConfiguration;
	private final YamlConfigParser fMotorsParser;
	private final YamlConfigParser fSolenoidsParser;
	private double fPreviousTime;
	private long MIN_FRAME_TIME;


	@Inject
	public OutputService(ModelFactory modelFactory, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fRobotConfiguration = robotConfiguration;
		fSharedOutputsDirectory = objectsDirectory;
		fMotorsParser = new YamlConfigParser();
		fSolenoidsParser = new YamlConfigParser();
		fPreviousTime = -1;
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting OutputService");

		fMotorsParser.loadWithFolderName("motors.yaml");
		fSolenoidsParser.loadWithFolderName("solenoids.yaml");
		fSharedOutputsDirectory.registerAllOutputs(fMotorsParser, fSolenoidsParser);

		MIN_FRAME_TIME = fRobotConfiguration.getInt("global_timing", "ouput_service_max_frame_time");

		sLogger.debug("OutputService started");
	}

	@Override
	protected void runOneIteration() throws Exception {
		for (String motorName : fRobotConfiguration.getMotorNames()) {
			Motor motorObject = fSharedOutputsDirectory.getMotorObject(motorName);
			motorObject.setHardware(fSharedOutputValues.getMotorType(motorName), fSharedOutputValues.getMotorOutputValue(motorName), fSharedOutputValues.getMotorFlag(motorName));
			//fSharedOutputValues.putMotorCurrentValues(motorName, motorObject.getMotorCurrentValues());
		}
		for (String solenoidName : fRobotConfiguration.getSolenoidNames()) {
			Solenoid solenoidObject = fSharedOutputsDirectory.getSolenoidObject(solenoidName);
			solenoidObject.setHardware(fSharedOutputValues.getSolenoidOutputValue(solenoidName));
		}
		// Check for delayed frames
		fPreviousTime = (fPreviousTime < 0) ? System.currentTimeMillis() : fPreviousTime;
		double currentTime = System.currentTimeMillis();
		double diffTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_output_service_frame_time", diffTime);
		if (diffTime > MIN_FRAME_TIME) {
			sLogger.info("********** Output Service frame time = {}", diffTime);
		}
		fPreviousTime = currentTime;
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
	}
}
