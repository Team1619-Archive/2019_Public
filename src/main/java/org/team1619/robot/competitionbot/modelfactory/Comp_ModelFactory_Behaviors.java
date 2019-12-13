package org.team1619.robot.competitionbot.modelfactory;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.robot.competitionbot.behavior.*;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

public class Comp_ModelFactory_Behaviors {

	private static final Logger sLogger = LogManager.getLogger(Comp_ModelFactory_Behaviors.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private final RobotConfiguration fRobotConfiguration;

	public Comp_ModelFactory_Behaviors(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fRobotConfiguration = robotConfiguration;
	}

	public Behavior createBehavior(String name, Config config) {
		sLogger.debug("Creating behavior '{}' of type '{}' with config '{}'", name, config.getType(), config.getData());

		switch (name) {
			case "bh_drive_zero":
				return new Comp_Drive_Zero(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_drive_percent_auto":
				return new Comp_Drive_Percent_Auto(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_elevator_states":
				return new Comp_Elevator_States(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration, fDashboard);
			case "bh_elevator_zero":
				return new Comp_Elevator_Zero(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_elevator_manual":
				return new Comp_Elevator_Manual(fSharedInputValues, fSharedOutputValues, config, fRobotConfiguration);
			case "bh_drive_percent":
				return new Comp_Drive_Percent(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_drive_pure_pursuit":
				return new Comp_Drive_Pure_Pursuit(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_drive_lldirect":
				return new Comp_Drive_LLDirect(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_drive_lldirect_auto":
				return new Comp_Drive_LLDirect_Auto(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_ball_collector_manual":
				return new Comp_BallCollector_Manual(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_ball_collector_zero":
				return new Comp_BallCollector_Zero(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_ball_collector_states":
				return new Comp_BallCollector_States(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_crossbow_manual":
				return new Comp_Crossbow_Manual(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_crossbow_states":
				return new Comp_Crossbow_States(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_crossbow_zero":
				return new Comp_Crossbow_Zero(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_climber_manual":
				return new Comp_Climber_Manual(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_climber_zero":
				return new Comp_Climber_Zero(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_climber_states":
				return new Comp_Climber_States(fSharedInputValues, fSharedOutputValues, config, fDashboard, fRobotConfiguration);
			case "bh_drive_straight":
				return new Comp_DriveStraight(fSharedInputValues, fSharedOutputValues, fDashboard, fRobotConfiguration);
			default:
				throw new ConfigurationException("Behavior " + name + " does not exist.");
		}
	}

}