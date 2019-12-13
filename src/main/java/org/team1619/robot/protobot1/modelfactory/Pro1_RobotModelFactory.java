package org.team1619.robot.protobot1.modelfactory;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.robot.RobotModelFactory;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.Config;

public class Pro1_RobotModelFactory extends RobotModelFactory {

	private static final Logger sLogger = LogManager.getLogger(Pro1_RobotModelFactory.class);

	private final Pro1_ModelFactory_Behaviors fBehaviors;

	@Inject
	public Pro1_RobotModelFactory(InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		super(inputValues, outputValues, dashboard, robotConfiguration, objectsDirectory);
		fBehaviors = new Pro1_ModelFactory_Behaviors(inputValues, outputValues, dashboard, robotConfiguration);
	}

	@Override
	public Behavior createBehavior(String name, Config config) {
		return fBehaviors.createBehavior(name, config);
	}

}