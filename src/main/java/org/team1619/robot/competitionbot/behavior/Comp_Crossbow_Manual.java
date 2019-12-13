package org.team1619.robot.competitionbot.behavior;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

/**
 * Manually Extends and retracts the crossbow
 * Manually Retracts the crossbow wings
 */

public class Comp_Crossbow_Manual implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Crossbow_Manual.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_crossbow");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private String fMainSolenoidButton;
	private String fWingsButton;

	public Comp_Crossbow_Manual(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fMainSolenoidButton = robotConfiguration.getString("global_crossbow", "main_solenoid_button");
		fWingsButton = robotConfiguration.getString("global_crossbow", "wings_button");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
	}

	@Override
	public void update() {

		boolean extendWings = fSharedInputValues.getBoolean(fWingsButton);
		boolean extendMainSolenoid = fSharedInputValues.getBoolean(fMainSolenoidButton);

		fSharedOutputValues.setSolenoidOutputValue("so_crossbow_main", extendMainSolenoid);
		fSharedOutputValues.setSolenoidOutputValue("so_crossbow_wings", extendWings);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
	}

	@Override
	public boolean isDone() {
		return true;
	}

	@Override
	public boolean isRequestingDisposal() {
		return false;
	}

	@Override
	public ImmutableSet<String> getSubsystems() {
		return sSubsystems;
	}

}
