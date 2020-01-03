package org.team1619.services.input;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.inputs.bool.BooleanInput;
import org.team1619.models.inputs.numeric.NumericInput;
import org.team1619.models.inputs.vector.VectorInput;
import org.team1619.robot.ModelFactory;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.YamlConfigParser;
import org.team1619.utilities.services.ScheduledService;
import org.team1619.utilities.services.Scheduler;

public class InputService extends ScheduledService {

	private static final Logger sLogger = LogManager.getLogger(InputService.class);

	private final InputValues fSharedInputValues;
	private final ObjectsDirectory fSharedObjectsDirectory;
	private final RobotConfiguration fRobotConfiguration;
	private final YamlConfigParser fBooleanParser;
	private final YamlConfigParser fNumericParser;
	private final YamlConfigParser fVectorParser;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;
	private long FRAME_CYCLE_TIME_THRESHOLD;

	@Inject
	public InputService(ModelFactory modelFactory, InputValues inputValues, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;
		fSharedObjectsDirectory = objectsDirectory;
		fBooleanParser = new YamlConfigParser();
		fNumericParser = new YamlConfigParser();
		fVectorParser = new YamlConfigParser();
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting InputService");

		fBooleanParser.loadWithFolderName("boolean-inputs.yaml");
		fNumericParser.loadWithFolderName("numeric-inputs.yaml");
		fVectorParser.loadWithFolderName("vector-inputs.yaml");
		fSharedObjectsDirectory.registerAllInputs(fBooleanParser, fNumericParser, fVectorParser);
		fPreviousTime = -1;
		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_input_service");
		FRAME_CYCLE_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_input_service");

		fSharedInputValues.setString("active states", "");

		sLogger.info("InputService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

		for (String name : fRobotConfiguration.getBooleanInputNames()) {
			BooleanInput booleanInput = fSharedObjectsDirectory.getBooleanInputObject(name);
			booleanInput.update();
			fSharedInputValues.setBoolean(name, booleanInput.get(fSharedInputValues.getInputFlag(name)));
			if (booleanInput.getDelta() == BooleanInput.DeltaType.RISING_EDGE) {
				fSharedInputValues.setBooleanRisingEdge(name, true);
			}
		}

		//sLogger.debug("Updated boolean inputs");

		for (String name : fRobotConfiguration.getNumericInputNames()) {
			NumericInput numericInput = fSharedObjectsDirectory.getNumericInputObject(name);
			numericInput.update();
			//double delta = numericInput.getDelta();
			fSharedInputValues.setNumeric(name, numericInput.get(fSharedInputValues.getInputFlag(name)));
		}

		//sLogger.debug("Updated numeric inputs");

		for (String name : fRobotConfiguration.getVectorInputNames()) {
			VectorInput vectorInput = fSharedObjectsDirectory.getVectorInputObject(name);
			vectorInput.update();
			fSharedInputValues.setVector(name, vectorInput.get(fSharedInputValues.getInputFlag(name)));
		}

		//sLogger.debug("Updated vector inputs");

		// Check for delayed frames
		double currentTime = System.currentTimeMillis();
		double frameTime = currentTime - frameStartTime;
		double totalCycleTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_frame_time_input_service", frameTime);
		fSharedInputValues.setNumeric("ni_frame_cycle_time_input_service", totalCycleTime);
		if (frameTime > FRAME_TIME_THRESHOLD) {
			sLogger.info("********** Input Service frame time = {}", frameTime);
		}
		if (totalCycleTime > FRAME_CYCLE_TIME_THRESHOLD) {
			sLogger.info("********** Input Service frame cycle time = {}", totalCycleTime);
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

	public void broadcast() {
		sLogger.info("Broadcasting input values");

		for (String name : fRobotConfiguration.getBooleanInputNames()) {
			BooleanInput booleanInput = fSharedObjectsDirectory.getBooleanInputObject(name);
			fSharedInputValues.setBoolean(name, booleanInput.get(fSharedInputValues.getInputFlag(name)));
		}

		for (String name : fRobotConfiguration.getNumericInputNames()) {
			NumericInput numericInput = fSharedObjectsDirectory.getNumericInputObject(name);
			fSharedInputValues.setNumeric(name, numericInput.get(fSharedInputValues.getInputFlag(name)));
		}

		for (String name : fRobotConfiguration.getVectorInputNames()) {
			VectorInput vectorInput = fSharedObjectsDirectory.getVectorInputObject(name);
			fSharedInputValues.setVector(name, vectorInput.get(fSharedInputValues.getInputFlag(name)));
		}
	}
}
