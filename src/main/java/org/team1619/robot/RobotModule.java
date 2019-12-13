package org.team1619.robot;

import org.team1619.robot.competitionbot.modelfactory.Comp_RobotModelFactory;
import org.team1619.robot.competitionbot.state.Comp_AutoStateControls;
import org.team1619.robot.competitionbot.state.Comp_TeleopStateControls;
import org.team1619.robot.protobot1.modelfactory.Pro1_RobotModelFactory;
import org.team1619.robot.protobot1.state.Pro1_AutoStateControls;
import org.team1619.robot.protobot1.state.Pro1_TeleopStateControls;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.concretions.robot.RobotDashboard;

public class RobotModule extends Module {
	private String fRobotName;

	public RobotModule(String robotName) {
		fRobotName = robotName;
	}

	@Override
	public void configureModeSpecificConcretions() {
		bind(Dashboard.class, RobotDashboard.class);

		switch (fRobotName) {
			case "competitionbot": {
				bind(ModelFactory.class, Comp_RobotModelFactory.class);
				bind(TeleopStateControls.class, Comp_TeleopStateControls.class);
				bind(AutoStateControls.class, Comp_AutoStateControls.class);
				break;
			}
			case "protobot1": {
				bind(ModelFactory.class, Pro1_RobotModelFactory.class);
				bind(TeleopStateControls.class, Pro1_TeleopStateControls.class);
				bind(AutoStateControls.class, Pro1_AutoStateControls.class);
				break;
			}
		}
	}
}
