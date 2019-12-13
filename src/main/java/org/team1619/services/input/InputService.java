package org.team1619.services.input;

import com.google.common.util.concurrent.AbstractScheduledService;
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

import java.util.concurrent.TimeUnit;

public class InputService extends AbstractScheduledService {


	private static final Logger sLogger = LogManager.getLogger(InputService.class);

	private final InputValues fSharedInputValues;
	private final ObjectsDirectory fSharedObjectsDirectory;
	private final RobotConfiguration fRobotConfiguration;
	private final YamlConfigParser fBooleanParser;
	private final YamlConfigParser fNumericParser;
	private final YamlConfigParser fVectorParser;
	private double fPreviousTime;
	private long MIN_FRAME_TIME;

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
		MIN_FRAME_TIME = fRobotConfiguration.getInt("global_timing", "input_service_max_frame_time");

		fSharedInputValues.setString("active states", "");

		sLogger.info("InputService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

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
		fPreviousTime = (fPreviousTime < 0) ? System.currentTimeMillis() : fPreviousTime;
		double currentTime = System.currentTimeMillis();
		double diffTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_input_service_frame_time", diffTime);
		if (diffTime > MIN_FRAME_TIME) {
			sLogger.info("********** Input Service frame time = {}", diffTime);
		}
		fPreviousTime = currentTime;

	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
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
