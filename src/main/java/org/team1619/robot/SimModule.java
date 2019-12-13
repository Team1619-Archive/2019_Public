package org.team1619.robot;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.robot.competitionbot.modelfactory.Comp_SimModelFactory;
import org.team1619.robot.competitionbot.state.Comp_AutoStateControls;
import org.team1619.robot.competitionbot.state.Comp_TeleopStateControls;
import org.team1619.robot.protobot1.modelfactory.Pro1_SimModelFactory;
import org.team1619.robot.protobot1.state.Pro1_AutoStateControls;
import org.team1619.robot.protobot1.state.Pro1_TeleopStateControls;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.sim.SimDashboard;

public class SimModule extends Module {
	private static final Logger sLogger = LogManager.getLogger(Module.class);

	private String fRobotName;

	public SimModule(String robotName) {
		fRobotName = robotName;
	}

	@Override
	public void configureModeSpecificConcretions() {

		bind(Dashboard.class, SimDashboard.class);

		switch (fRobotName) {
			case "competitionbot": {
				bind(ModelFactory.class, Comp_SimModelFactory.class);
				bind(TeleopStateControls.class, Comp_TeleopStateControls.class);
				bind(AutoStateControls.class, Comp_AutoStateControls.class);
				break;
			}
			case "protobot1": {
				bind(ModelFactory.class, Pro1_SimModelFactory.class);
				bind(TeleopStateControls.class, Pro1_TeleopStateControls.class);
				bind(AutoStateControls.class, Pro1_AutoStateControls.class);
				break;
			}
		}
	}
}
