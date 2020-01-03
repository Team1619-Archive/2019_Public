package org.team1619.services.webdashboard.sim;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.services.ScheduledService;
import org.team1619.utilities.services.Scheduler;

/**
 * SimWebDashboardService is the service the creates and runs the webdashboard client in sim mode
 *
 * @author Matthew Oates
 */

public class SimWebDashboardService extends ScheduledService {

	private static final Logger sLogger = LogManager.getLogger(SimWebDashboardService.class);

	private final InputValues fSharedInputValues;
	private final RobotConfiguration fRobotConfiguration;
	private SimWebDashboardServer fWebDashboardClient;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;
	private long FRAME_CYCLE_TIME_THRESHOLD;

	@Inject
	public SimWebDashboardService(EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;
		fWebDashboardClient = new SimWebDashboardServer(5800, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting SimWebDashboardService");
		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_webdashboard_service");
		FRAME_CYCLE_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_webdashboard_service");

		fWebDashboardClient.start();
		fPreviousTime = -1;
		sLogger.info("SimWebDashboardService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

		fWebDashboardClient.update();

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
		sLogger.info("Shutting down SimWebDashboardService");

		fWebDashboardClient.stop();

		sLogger.info("SimWebDashboardService shut down");
	}

	@Override
	protected Scheduler scheduler() {
		return new Scheduler(1000 / 60);
	}
}
