package org.team1619.robot;

import org.team1619.utilities.injection.AbstractModule;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.*;
import org.team1619.shared.concretions.*;

public abstract class Module extends AbstractModule {
	private static final Logger sLogger = LogManager.getLogger(Module.class);

	protected void configure() {
		bind(EventBus.class, SharedEventBus.class);
		bind(InputValues.class, SharedInputValues.class);
		bind(OutputValues.class, SharedOutputValues.class);
		bind(FMS.class, SharedFMS.class);
		bind(RobotConfiguration.class, SharedRobotConfiguration.class);
		bind(ObjectsDirectory.class, SharedObjectsDirectory.class);

		configureModeSpecificConcretions();
	}

	public abstract void configureModeSpecificConcretions();
}

