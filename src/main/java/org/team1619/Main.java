package org.team1619;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ServiceManager;
import org.team1619.robot.SimModule;
import org.team1619.services.input.InputService;
import org.team1619.services.logging.LoggingService;
import org.team1619.services.output.OutputService;
import org.team1619.services.states.StatesService;
import org.team1619.services.webdashboard.sim.SimWebDashboardService;
import org.team1619.shared.concretions.SharedRobotConfiguration;
import org.team1619.utilities.Config;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.utilities.YamlConfigParser;
import org.team1619.utilities.injection.Injector;

public class Main {

	private static final Logger sLogger = LogManager.getLogger(Main.class);

	public static void main(String[] args) {

		System.setProperty("logPath", "logs");

		YamlConfigParser parser = new YamlConfigParser();
		parser.load("general.yaml");
		Config config = parser.getConfig("robot");
		String robotName = (String) config.get("robot_name");

		Injector injector = new Injector(new SimModule(robotName));
		injector.getInstance(SharedRobotConfiguration.class).initialize();

		sLogger.info("------ ROBOT CONFIGURATION = " + robotName + " -----------");

		StatesService statesService = injector.getInstance(StatesService.class);
		statesService.setRobotName(robotName);
		InputService inputService = injector.getInstance(InputService.class);
		OutputService outputService = injector.getInstance(OutputService.class);
		LoggingService loggingService = injector.getInstance(LoggingService.class);
		//SimInputSocketListenerService simInputSocketListenerService = injector.getInstance(SimInputSocketListenerService.class);
		SimWebDashboardService simWebDashboardService = injector.getInstance(SimWebDashboardService.class);

		ServiceManager serviceManager = new ServiceManager(ImmutableSet.of(statesService, inputService, outputService, loggingService, simWebDashboardService));
		sLogger.info("Starting services");
		serviceManager.startAsync();
		serviceManager.awaitHealthy();
		inputService.broadcast();
		sLogger.info("********************* ALL SERVICES STARTED *******************************");
		serviceManager.awaitStopped();
		sLogger.info("All Services stopped");
	}
}
