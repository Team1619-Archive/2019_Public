package org.team1619.services.webdashboard.robot;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.services.webdashboard.WebDashboardServer;
import org.team1619.shared.abstractions.*;

import java.util.concurrent.TimeUnit;

/**
 * RobotWebDashboardService is the service the creates and runs the webdashboard client on the robot
 *
 * @author Matthew Oates
 */

public class RobotWebDashboardService extends AbstractScheduledService {

	private static final Logger sLogger = LogManager.getLogger(RobotWebDashboardService.class);

	private final InputValues fSharedInputValues;
	private final RobotConfiguration fRobotConfiguration;
	private WebDashboardServer fWebDashboardServer;

	@Inject
	public RobotWebDashboardService(EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;

		fWebDashboardServer = new RobotWebDashboardServer(5800, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting RobotWebDashboardService");

		fWebDashboardServer.start();

		sLogger.info("RobotWebDashboardService started");
	}

	@Override
	protected void runOneIteration() throws Exception {
		fWebDashboardServer.update();
	}

	@Override
	protected void shutDown() throws Exception {
		sLogger.info("Shutting down RobotWebDashboardService");

		fWebDashboardServer.stop();

		sLogger.info("RobotWebDashboardService shut down");
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
	}
}
