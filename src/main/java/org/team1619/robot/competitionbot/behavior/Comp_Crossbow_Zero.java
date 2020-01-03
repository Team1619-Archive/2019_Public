package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;
import org.team1619.utilities.Timer;
import java.util.Set;

/**
 * Zeros the crossbow
 */

public class Comp_Crossbow_Zero implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Crossbow_Zero.class);
	private static final Set<String> sSubsystems = Set.of("ss_crossbow");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;
	private String fRetractWingsButton;
	private String fWingsPosition;

	private long MAIN_SOLENOID_WAIT_TIME;

	private String fCurrentPosition;
	private final Timer fTimerMainSolenoid = new Timer();


	public Comp_Crossbow_Zero(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fCurrentPosition = "retracted";
		fWingsPosition = "extended";

		MAIN_SOLENOID_WAIT_TIME = robotConfiguration.getInt("global_crossbow", "main_solenoid_wait_time");
		fRetractWingsButton = robotConfiguration.getString("global_crossbow", "wings_button");
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fTimerMainSolenoid.reset();

	}

	@Override
	public void update() {

		if(!fSharedInputValues.getBoolean("bi_crossbow_has_been_zeroed")){
			if(!fTimerMainSolenoid.isStarted()) {
				fSharedOutputValues.setSolenoidOutputValue("so_crossbow_main", false);
				fTimerMainSolenoid.start(MAIN_SOLENOID_WAIT_TIME);
				fCurrentPosition = "transitioning";
			} else if(fTimerMainSolenoid.isDone()) {
				fCurrentPosition = "retracted";
				sLogger.info("Crossbow Zero -> Zeroed");
				fSharedInputValues.setBoolean("bi_crossbow_has_been_zeroed", true);
			}
		}
		fSharedInputValues.setString("si_crossbow_position", fCurrentPosition);

		//Retract wings?
		boolean retractWings = fSharedInputValues.getBoolean(fRetractWingsButton);
		if (retractWings) {
			fSharedOutputValues.setSolenoidOutputValue("so_crossbow_wings", true);
			fWingsPosition = "retracted";
		} else {
			fSharedOutputValues.setSolenoidOutputValue("so_crossbow_wings", false);
			fWingsPosition = "extended";
		}

		fSharedInputValues.setString("si_crossbow_wings_position", fWingsPosition);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
	}

	@Override
	public boolean isDone() {
		return fSharedInputValues.getBoolean("bi_crossbow_has_been_zeroed");
	}

	@Override
	public boolean isRequestingDisposal() {
		return false;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}

}
