package org.team1619.services.webdashboard.sim;

import org.team1619.services.webdashboard.WebDashboardServer;
import org.team1619.shared.abstractions.*;

/**
 * SimWebDashboardServer extends WebDashboardServer for use in sim mode
 *
 * @author Matthew Oates
 */

public class SimWebDashboardServer extends WebDashboardServer {

	public SimWebDashboardServer(int port, EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		super(port, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}
}
