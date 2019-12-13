package org.team1619.robot.protobot1.modelfactory;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.robot.protobot1.behavior.Pro1_Drive_Percent;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

public class Pro1_ModelFactory_Behaviors {

	private static final Logger sLogger = LogManager.getLogger(Pro1_ModelFactory_Behaviors.class);

	private InputValues fSharedInputValues;
	private OutputValues fSharedOutputValues;
	private Dashboard fDashboard;
	private RobotConfiguration fSharedRobotConfiguration;

	public Pro1_ModelFactory_Behaviors(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fSharedRobotConfiguration = robotConfiguration;
	}

	public Behavior createBehavior(String name, Config config) {
		sLogger.debug("Creating behavior '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (name) {
			case "bh_drive_percent":
				return new Pro1_Drive_Percent(fSharedInputValues, fSharedOutputValues, config, fSharedRobotConfiguration);
			default:
				throw new ConfigurationException("Behavior " + name + " does not exist.");
		}
	}

}