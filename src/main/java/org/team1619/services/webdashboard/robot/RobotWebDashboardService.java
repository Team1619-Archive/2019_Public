package org.team1619.services.webdashboard.robot;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.services.webdashboard.WebDashboardServer;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.services.ScheduledService;
import org.team1619.utilities.services.Scheduler;

/**
 * RobotWebDashboardService is the service the creates and runs the webdashboard client on the robot
 *
 * @author Matthew Oates
 */

public class RobotWebDashboardService extends ScheduledService {

	private static final Logger sLogger = LogManager.getLogger(RobotWebDashboardService.class);

	private final InputValues fSharedInputValues;
	private final RobotConfiguration fRobotConfiguration;
	private WebDashboardServer fWebDashboardServer;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;
	private long FRAME_CYCLE_TIME_THRESHOLD;


	@Inject
	public RobotWebDashboardService(EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;

		fWebDashboardServer = new RobotWebDashboardServer(5800, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting RobotWebDashboardService");
		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_webdashboard_service");
		FRAME_CYCLE_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_webdashboard_service");
		fWebDashboardServer.start();
		fPreviousTime = -1;
		sLogger.info("RobotWebDashboardService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

		fWebDashboardServer.update();

		// Check for delayed frames
		double currentTime = System.currentTimeMillis();
		double frameTime = currentTime - frameStartTime;
		double totalCycleTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_frame_time_webdashboard_service", frameTime);
		fSharedInputValues.setNumeric("ni_frame_cycle_time_webdashboard_service", totalCycleTime);
		if (frameTime > FRAME_TIME_THRESHOLD) {
			sLogger.info("********** WebDashboard Service frame time = {}", frameTime);
		}
		if (totalCycleTime > FRAME_CYCLE_TIME_THRESHOLD) {
			sLogger.info("********** WebDashboard Service frame cycle time = {}", totalCycleTime);
		}
		fPreviousTime = currentTime;
	}

	@Override
	protected void shutDown() throws Exception {
		sLogger.info("Shutting down RobotWebDashboardService");

		fWebDashboardServer.stop();

		sLogger.info("RobotWebDashboardService shut down");
	}

	@Override
	protected Scheduler scheduler() {
		return new Scheduler(1000 / 60);
	}
}
