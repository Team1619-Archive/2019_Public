package org.team1619.services.webdashboard.robot;

import org.team1619.services.webdashboard.WebDashboardServer;
import org.team1619.shared.abstractions.*;

/**
 * RobotWebDashboardServer extends WebDashboardServer for use on the robot
 *
 * @author Matthew Oates
 */

public class RobotWebDashboardServer extends WebDashboardServer {

	public RobotWebDashboardServer(int port, EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		super(port, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}
}
