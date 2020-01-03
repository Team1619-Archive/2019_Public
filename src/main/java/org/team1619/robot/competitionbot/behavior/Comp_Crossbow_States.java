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
 * Extends and retracts the crossbow
 * Retracts the crossbow wings
 */

public class Comp_Crossbow_States implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Crossbow_States.class);
	private static final Set<String> sSubsystems = Set.of("ss_crossbow");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private long MAIN_SOLENOID_WAIT_TIME;
	private double MAX_SAFE_BALL_COLLECTOR_POSITION;

	private String fCurrentPosition;
	private String fDesiredPosition;
	private String fWingsPosition;
	private String fRetractWingsButton;
	private boolean fWingsRetracted;
	private String fBallCollectorEncoder;
	private final Timer fTimerMainSolenoid = new Timer();


	public Comp_Crossbow_States(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fCurrentPosition = "retracted";
		fWingsPosition = "extended";
		fDesiredPosition = "retracted";

		MAX_SAFE_BALL_COLLECTOR_POSITION = robotConfiguration.getDouble("global_crossbow", "crossbow_max_safe_ball_collector_position");
		MAIN_SOLENOID_WAIT_TIME = robotConfiguration.getInt("global_crossbow", "main_solenoid_wait_time");
		fBallCollectorEncoder = robotConfiguration.getString("global_crossbow", "ball_encoder");
		fRetractWingsButton = robotConfiguration.getString("global_crossbow", "wings_button");
		fWingsRetracted = false;
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fWingsRetracted = config.getBoolean("wings_retracted", false);
		fCurrentPosition = fSharedInputValues.getString("si_crossbow_position");
		fDesiredPosition = config.getString("position");
		fTimerMainSolenoid.reset();
	}

	@Override
	public void update() {

		String ballCollectorCurrentPosition = fSharedInputValues.getString("si_ball_collector_current_position");
		double ballCollectorCurrentEncoderPosition = fSharedInputValues.getNumeric(fBallCollectorEncoder);
		boolean isSafeToExtend = (((ballCollectorCurrentPosition.equals("stow")) || (ballCollectorCurrentPosition.equals("protect"))) && (ballCollectorCurrentEncoderPosition < MAX_SAFE_BALL_COLLECTOR_POSITION));
		boolean retractWings = fSharedInputValues.getBoolean(fRetractWingsButton) || fSharedInputValues.getString("si_ball_collector_current_position").equals("collect_floor");
//		boolean hasCargo = fSharedInputValues.getBoolean("bi_ball_collector_beam_sensor");

		//Retract wings?
		if (fWingsRetracted || retractWings) {
			fSharedOutputValues.setSolenoidOutputValue("so_crossbow_wings", true);
			fWingsPosition = "retracted";
		} else {
			fSharedOutputValues.setSolenoidOutputValue("so_crossbow_wings", false);
			fWingsPosition = "extended";
		}

		fSharedInputValues.setString("si_crossbow_wings_position", fWingsPosition);

		//Retract/Extend Crossbow Main Solenoid
		switch (fDesiredPosition) {
			case "extended":
				if (isSafeToExtend) {
					if (!fTimerMainSolenoid.isStarted() && (!fCurrentPosition.equals(fDesiredPosition))) {
						fTimerMainSolenoid.start(MAIN_SOLENOID_WAIT_TIME);
						fCurrentPosition = "transitioning";
						fSharedOutputValues.setSolenoidOutputValue("so_crossbow_main", true);
					}
				}
				break;
			case "retracted":
				if (!fTimerMainSolenoid.isStarted() && (!fCurrentPosition.equals(fDesiredPosition))) {
					fTimerMainSolenoid.start(MAIN_SOLENOID_WAIT_TIME);
					fCurrentPosition = "transitioning";
					fSharedOutputValues.setSolenoidOutputValue("so_crossbow_main", false);
				}
				break;
		}


		//Wait long enough for the solenoid to complete motion
		if (fTimerMainSolenoid.isDone() && fCurrentPosition.equals("transitioning")) {
			fCurrentPosition = fDesiredPosition;
			fTimerMainSolenoid.reset();
			sLogger.info("Crossbow -> Current position  = {}, Desired Position = {}, Wings = {}", fCurrentPosition, fDesiredPosition, fWingsPosition);
		}
		fSharedInputValues.setString("si_crossbow_position", fCurrentPosition);
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
	}

	@Override
	public boolean isDone() {
		return fCurrentPosition.equals(fDesiredPosition);
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
