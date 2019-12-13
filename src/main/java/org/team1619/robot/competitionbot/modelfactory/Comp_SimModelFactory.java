package org.team1619.robot.competitionbot.modelfactory;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.robot.SimModelFactory;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.Config;

public class Comp_SimModelFactory extends SimModelFactory {

	private static final Logger sLogger = LogManager.getLogger(Comp_SimModelFactory.class);

	private final Comp_ModelFactory_Behaviors fBehaviors;

	@Inject
	public Comp_SimModelFactory(EventBus eventBus, InputValues inputValues, OutputValues outputValues, Dashboard dashboard, RobotConfiguration robotConfiguration, ObjectsDirectory objectsDirectory) {
		super(eventBus, inputValues, outputValues, dashboard, robotConfiguration, objectsDirectory);
		fBehaviors = new Comp_ModelFactory_Behaviors(inputValues, outputValues, dashboard, robotConfiguration);
	}


	@Override
	public Behavior createBehavior(String name, Config config) {
		return fBehaviors.createBehavior(name, config);
	}

}