package org.team1619.services.states;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.robot.AutoStateControls;
import org.team1619.robot.TeleopStateControls;
import org.team1619.shared.abstractions.FMS;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.ObjectsDirectory;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.YamlConfigParser;

import java.util.concurrent.TimeUnit;

/**
 * Reads the FMS mode sent to us by the field and runs the correct StateControls and StateMachine
 */

public class StatesService extends AbstractScheduledService {

	private static final Logger sLogger = LogManager.getLogger(StatesService.class);
	private final ObjectsDirectory fSharedObjectsDirectory;
	private final InputValues fSharedInputValues;
	private final FMS fFms;
	private final YamlConfigParser fParser;
	private final RobotConfiguration fRobotConfiguration;
	private double fPreviousTime;
	private long MIN_FRAME_TIME;

	private FMS.Mode fCurrentFmsMode;
	private String fRobotName = "competitionbot";
	private StateMachine fStateMachine;
	private TeleopStateControls fTeleopStateControls;
	private AutoStateControls fAutoStateControls;

	@Inject
	public StatesService(InputValues inputValues, FMS fms, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory, TeleopStateControls teleopStateControls, AutoStateControls autoStateControls) {
		fParser = new YamlConfigParser();
		fSharedObjectsDirectory = objectsDirectory;
		fSharedInputValues = inputValues;
		fFms = fms;
		fCurrentFmsMode = fFms.getMode();
		fRobotConfiguration = robotConfiguration;
		fTeleopStateControls = teleopStateControls;
		fAutoStateControls = autoStateControls;
		fStateMachine = new StateMachine(fSharedObjectsDirectory, fTeleopStateControls, fRobotConfiguration, fSharedInputValues);
	}

	/**
	 * Called when the code is stated up by the abstractSchedulerService
	 * @throws Exception if it does not start up correctly
	 */
	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting StatesService");

		fParser.loadWithFolderName("states.yaml");
		fSharedObjectsDirectory.registerAllStates(fParser);

		MIN_FRAME_TIME = fRobotConfiguration.getInt("global_timing", "state_service_max_frame_time");
		fPreviousTime = -1;
		fSharedInputValues.setBoolean("bi_has_been_zeroed", false);

		sLogger.info("StatesService started");
	}

	/**
	 * Sets the 'robot' we are currently running
	 * @param robotName the name of the robot specified in robotconfigruation.ymal
	 */
	public void setRobotName(String robotName) {
		fRobotName = robotName;
	}

	/**
	 * Determines the frame rate for this service
	 * @return the frame rate
	 */
	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
	}

	/**
	 * Called every frame by the abstractSchedulerService based on the frame time set in scheduler() above
	 * Decides what mode we are running (Auto, Teleop, Disabled)
	 * Updates the instance of StateControls and the StateMachine
	 */
	@Override
	protected void runOneIteration() {

		//Get the FMS mode from the field or webDashboard
		FMS.Mode nextFmsMode = fFms.getMode();

		//If we are currently in Auto and auto has completed switch to teleop
		if((nextFmsMode == FMS.Mode.AUTONOMOUS || fCurrentFmsMode == FMS.Mode.AUTONOMOUS) && fSharedInputValues.getBoolean("bi_auto_complete")){
			nextFmsMode = FMS.Mode.TELEOP;
		}

		//If mode is changing
		if (nextFmsMode != fCurrentFmsMode) {

			//Dispose of the current state controls
			if(fCurrentFmsMode == FMS.Mode.TELEOP){
				sLogger.info("Ending teleop");
				fTeleopStateControls.dispose();
			}else if(fCurrentFmsMode == FMS.Mode.AUTONOMOUS ){
				sLogger.info("Ending autonomous");
				fAutoStateControls.dispose();
			}

			// Initialize StateMachine with either Teleop or Auto StateControls
			if (nextFmsMode == FMS.Mode.TELEOP) {
				sLogger.info("Starting teleop");
				fTeleopStateControls.initialize();
				fStateMachine.initialize(fTeleopStateControls);
			} else if (nextFmsMode == FMS.Mode.AUTONOMOUS) {
				sLogger.info("Starting autonomous");
				fAutoStateControls.initialize();
				fStateMachine.initialize(fAutoStateControls);
			} else if (nextFmsMode == FMS.Mode.DISABLED){
				sLogger.info("Disabled");
				fStateMachine.dispose();
				fSharedInputValues.setBoolean("bi_has_been_zeroed", false);
				fSharedInputValues.setBoolean("bi_auto_complete", false);
			}
		}

		//Update the current stateControls and update the stateMachine
		fCurrentFmsMode = nextFmsMode;
		if (fCurrentFmsMode == FMS.Mode.TELEOP) {
			fTeleopStateControls.update();
			fStateMachine.update();
		} else if (fCurrentFmsMode == FMS.Mode.AUTONOMOUS) {
			fAutoStateControls.update();
			fStateMachine.update();
		}

		// Check for delayed frames
		fPreviousTime = (fPreviousTime < 0) ? System.currentTimeMillis() : fPreviousTime;
		double currentTime = System.currentTimeMillis();
		double diffTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_state_service_frame_time", diffTime);
		if (diffTime > MIN_FRAME_TIME) {
			sLogger.info("********** State Service frame time = {}", diffTime);
		}
		fPreviousTime = currentTime;
	}
}
