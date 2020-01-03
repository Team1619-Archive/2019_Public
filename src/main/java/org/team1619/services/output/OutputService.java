package org.team1619.services.output;

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
import org.team1619.utilities.services.ScheduledService;
import org.team1619.utilities.services.Scheduler;
import java.util.Map;

public class OutputService extends ScheduledService {

	private static final Logger sLogger = LogManager.getLogger(OutputService.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final ObjectsDirectory fSharedOutputsDirectory;
	private final RobotConfiguration fRobotConfiguration;
	private final YamlConfigParser fMotorsParser;
	private final YamlConfigParser fSolenoidsParser;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;
	private long FRAME_CYCLE_TIME_THRESHOLD;


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

		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_output_service");
		FRAME_CYCLE_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_output_service");

		sLogger.debug("OutputService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

		for (String motorName : fRobotConfiguration.getMotorNames()) {
			Motor motorObject = fSharedOutputsDirectory.getMotorObject(motorName);
			Map<String, Object> motorOutputs = fSharedOutputValues.getMotorOutputs(motorName);
			motorObject.setHardware((Motor.OutputType) motorOutputs.get("type"), (double) motorOutputs.get("value"), motorOutputs.get("flag"));
			//fSharedOutputValues.putMotorCurrentValues(motorName, motorObject.getMotorCurrentValues());
		}
		for (String solenoidName : fRobotConfiguration.getSolenoidNames()) {
			Solenoid solenoidObject = fSharedOutputsDirectory.getSolenoidObject(solenoidName);
			solenoidObject.setHardware(fSharedOutputValues.getSolenoidOutputValue(solenoidName));
		}

		// Check for delayed frames
		double currentTime = System.currentTimeMillis();
		double frameTime = currentTime - frameStartTime;
		double totalCycleTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_frame_time_output_service", frameTime);
		fSharedInputValues.setNumeric("ni_frame_cycle_time_output_service", totalCycleTime);
		if (frameTime > FRAME_TIME_THRESHOLD) {
			sLogger.info("********** Output Service frame time = {}", frameTime);
		}
		if (totalCycleTime > FRAME_CYCLE_TIME_THRESHOLD) {
			sLogger.info("********** Output Service frame cycle time = {}", totalCycleTime);
		}
		fPreviousTime = currentTime;

	}

	@Override
	protected void shutDown() throws Exception {

	}

	@Override
	protected Scheduler scheduler() {
		return new Scheduler(1000 / 60);
	}
}
