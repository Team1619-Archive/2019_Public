package org.team1619.services.webdashboard.sim;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.*;

import java.util.concurrent.TimeUnit;

/**
 * SimWebDashboardService is the service the creates and runs the webdashboard client in sim mode
 *
 * @author Matthew Oates
 */

public class SimWebDashboardService extends AbstractScheduledService {

	private static final Logger sLogger = LogManager.getLogger(SimWebDashboardService.class);

	private final InputValues fSharedInputValues;

	private SimWebDashboardServer fWebDashboardClient;

	@Inject
	public SimWebDashboardService(EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;

		fWebDashboardClient = new SimWebDashboardServer(5800, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting SimWebDashboardService");

		fWebDashboardClient.start();

		sLogger.info("SimWebDashboardService started");
	}

	@Override
	protected void runOneIteration() throws Exception {
		fWebDashboardClient.update();
	}

	@Override
	protected void shutDown() throws Exception {
		sLogger.info("Shutting down SimWebDashboardService");

		fWebDashboardClient.stop();

		sLogger.info("SimWebDashboardService shut down");
	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
	}
}
