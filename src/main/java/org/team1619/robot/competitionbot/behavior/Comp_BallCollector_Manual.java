package org.team1619.robot.competitionbot.behavior;

import com.google.common.collect.ImmutableSet;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Config;

/**
 * allows you to run the pivot and rollers on the ball arm manually
 */

public class Comp_BallCollector_Manual implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_BallCollector_Manual.class);
	private static final ImmutableSet<String> sSubsystems = ImmutableSet.of("ss_ball_collector");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;

	private String fPivotAxis;
	private String fRollerTrigger;
	private double fRollerSpeed;
	private int fCurrentRollerState;
	private boolean fPreviousRollerTrigger;
	private double fIntakeRollerSpeed;
	private double fEjectRollerSpeed;
	private double fHoldRollerSpeed;

	public Comp_BallCollector_Manual(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "unknown";
		fPivotAxis = robotConfiguration.getString("global_ball_collector", "pivot_axis");
		fRollerTrigger = robotConfiguration.getString("global_ball_collector", "roller_trigger");
		fIntakeRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "intake_roller_speed");
		fEjectRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "eject_roller_speed");
		fHoldRollerSpeed = robotConfiguration.getDouble("global_ball_collector", "hold_roller_speed");
		fRollerSpeed = 0.0;
		fPreviousRollerTrigger = false;
		fCurrentRollerState = 0;

	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;
		fRollerSpeed = 0.0;
		fPreviousRollerTrigger = false;
	}

	@Override
	public void update() {
		double pivotAxis = fSharedInputValues.getNumeric(fPivotAxis);
		boolean rollerTrigger = fSharedInputValues.getBoolean(fRollerTrigger);

		//Step through roller speeds
		if (rollerTrigger && (rollerTrigger != fPreviousRollerTrigger)) {
			if (fCurrentRollerState == 0) {
				fRollerSpeed = fIntakeRollerSpeed;
				fCurrentRollerState = 1;
			} else if (fCurrentRollerState == 1) {
				fRollerSpeed = fHoldRollerSpeed;
				fCurrentRollerState = 2;
			} else if (fCurrentRollerState == 2) {
				fRollerSpeed = fEjectRollerSpeed;
				fCurrentRollerState = 3;
			} else {
				fRollerSpeed = 0.0;
				fCurrentRollerState = 0;
			}
		}
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_roller", Motor.OutputType.PERCENT, fRollerSpeed, null);
		fPreviousRollerTrigger = rollerTrigger;

		// Set pivot motor
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, pivotAxis, null);
	}


	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_pivot", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_ball_collector_roller", Motor.OutputType.PERCENT, 0.0, null);
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
