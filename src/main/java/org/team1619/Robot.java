package org.team1619;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.TimedRobot;
import org.team1619.robot.RobotModule;
import org.team1619.services.webdashboard.robot.RobotWebDashboardService;
import org.team1619.services.input.InputService;
import org.team1619.services.logging.LoggingService;
import org.team1619.services.output.OutputService;
import org.team1619.services.states.StatesService;
import org.team1619.shared.abstractions.FMS;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import org.team1619.utilities.YamlConfigParser;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.utilities.injection.Injector;
import org.team1619.utilities.services.Scheduler;
import org.team1619.utilities.services.managers.ScheduledLinearServiceManager;
import org.team1619.utilities.services.managers.ServiceManager;

public class Robot extends TimedRobot {

	private static final Logger sLogger = LogManager.getLogger(Robot.class);

	private Injector fInjector;
	private ServiceManager fServiceManager;
	private InputService fInputService;
	private FMS fFMS;

	public static void main(String... args) {
		System.setProperty("logPath", "/home/lvuser/logs");
		RobotBase.startRobot(Robot::new);
	}

	public Robot() {

		YamlConfigParser parser = new YamlConfigParser();
		parser.load("general.yaml");
		Config config = parser.getConfig("robot");
		String robotName = (String) config.get("robot_name");

		fInjector = new Injector(new RobotModule(robotName));

		sLogger.info("------ ROBOT CONFIGURATION = " + robotName + " -----------");
		StatesService statesService = fInjector.getInstance(StatesService.class);
		statesService.setRobotName(robotName);
		fInputService = fInjector.getInstance(InputService.class);
		OutputService outputService = fInjector.getInstance(OutputService.class);
		LoggingService loggingService = fInjector.getInstance(LoggingService.class);
		// TODO comment out to turn off webdashboard service
		RobotWebDashboardService robotWebDashboardService = fInjector.getInstance(RobotWebDashboardService.class);

		fServiceManager = new ScheduledLinearServiceManager(new Scheduler(30), fInputService, statesService, outputService, loggingService, robotWebDashboardService);

		// TODO comment in to when turning off webdashboard service
		//fServiceManager = new ScheduledLinearServiceManager(new Scheduler(30), Set.of(statesService, fInputService, outputService, loggingService));

		fFMS = fInjector.getInstance(FMS.class);
	}


	@Override
	public void robotInit() {
		sLogger.info("Initializing RobotConfiguration");
		fInjector.getInstance(RobotConfiguration.class).initialize();
		sLogger.info("Starting services");
		fServiceManager.start();
		fServiceManager.awaitHealthy();
		fInputService.broadcast();

		//NetworkTableInstance.getDefault().getTable("limelight").getEntry("stream").setNumber(2);
		UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
		camera.setFPS(15);
		camera.setResolution(176, 144);

		//camera.setFPS(20);
		//camera.setFPS(25);
		//camera.setFPS(30);
		//camera.setResolution(640, 480); // 4.2mbps at 15fps
		//camera.setResolution(352, 288); // 2.2mbps at 15fps
		//camera.setResolution(320, 240); // 1.8mbps at 15fps
		//camera.setResolution(176, 144); // .8mbps at 15fps

		sLogger.info("********************* ALL SERVICES STARTED *******************************");
	}

	@Override
	public void teleopInit() {
		fFMS.setMode(FMS.Mode.TELEOP);
	}

	@Override
	public void autonomousInit() {
		fFMS.setMode(FMS.Mode.AUTONOMOUS);
	}

	@Override
	public void disabledInit() {
		fFMS.setMode(FMS.Mode.DISABLED);
	}

	@Override
	public void testInit() {
		fFMS.setMode(FMS.Mode.TEST);
	}
}