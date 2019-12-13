package org.team1619.services.webdashboard;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.*;

/**
 * WebDashboardServer creates and manages a WebsocketServer on the correct ip
 *
 * @author Matthew Oates
 */

public abstract class WebDashboardServer {

	private static final Logger sLogger = LogManager.getLogger(WebDashboardServer.class);

	private WebHttpServer fWebHttpServer;
	private WebsocketServer fWebsocketServer;

	public WebDashboardServer(int port, EventBus eventBus, FMS fms, InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration) {
		fWebHttpServer = new WebHttpServer(port);
		fWebsocketServer = new WebsocketServer(port + 1, eventBus, fms, inputValues, outputValues, robotConfiguration);
	}

	public void start() {
		fWebsocketServer.initialize();
	}

	public void update() {
		fWebsocketServer.broadcastToWebDashboard();
	}

	public void stop() {

	}
}
