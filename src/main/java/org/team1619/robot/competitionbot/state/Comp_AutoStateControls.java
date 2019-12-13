package org.team1619.robot.competitionbot.state;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.state.State;
import org.team1619.robot.AutoStateControls;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 * Zeros all mechanisms and determines which auto to run
 */

public class Comp_AutoStateControls extends AutoStateControls {

	private static final Logger sLogger = LogManager.getLogger(Comp_AutoStateControls.class);
	private Dashboard fDashboard;

	private String fAutoOrigin;
	private String fAutoDestination;
	private String fAutoAction;
	private boolean fInterruptAutoButton;
	private String fCombinedAuto;


	@Inject
	public Comp_AutoStateControls(InputValues inputValues, RobotConfiguration robotConfiguration, Dashboard dashboard) {
		super(inputValues, robotConfiguration);
		fDashboard = dashboard;
		fAutoOrigin = "none";
		fAutoDestination = "none";
		fAutoAction = "none";
		fCombinedAuto = "none";
	}

	@Override
	public void initialize() {
		//Reads the values selected on the webdashboard and compiles them into the name of an auto.
		fAutoOrigin = (fSharedInputValues.getString("si_auto_origin").toLowerCase().replaceAll("\\s",""));
		fAutoDestination = (fSharedInputValues.getString("si_auto_destination").toLowerCase().replaceAll("\\s", ""));
		fAutoAction = (fSharedInputValues.getString("si_auto_action").toLowerCase().replaceAll("\\s", ""));
		fCombinedAuto = "sq_auto_" + fAutoOrigin + "_" + fAutoDestination + "_" + fAutoAction;
		sLogger.debug(fCombinedAuto);

		fSharedInputValues.setBoolean("bi_auto_complete", false);
		fInterruptAutoButton = false;

		//Zero everything
		if (!fSharedInputValues.getBoolean("bi_has_been_zeroed")) {
			fSharedInputValues.setBoolean("bi_elevator_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_climber_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_ball_collector_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_crossbow_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_drive_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_has_been_zeroed", true);
			fSharedInputValues.getVector("vi_navx", "zero");
		}

	}



	// These are states that should be allowed to complete once they have started.
	// Only include states that cannot get stuck and never complete
	@Override
	public boolean doNotInterrupt(String name) {
		return false;
	}

	// These states will always be active if no other states wants their subsystem
	@Override
	public boolean isDefaultState(String name) {
		switch (name) {
		}
		return false;
	}

	@Override
	public void update() {
		if(!fInterruptAutoButton && fSharedInputValues.getBoolean("bi_driver_back") ){
			fInterruptAutoButton = true;
			fSharedInputValues.setBoolean("bi_auto_complete", true);
		}
	}

	@Override
	public boolean isReady(String name) {
		//Check isReady on zero and idle states
		switch (name) {
			case "st_drive_zero":
				return !fSharedInputValues.getBoolean("bi_drive_has_been_zeroed");
			case "st_elevator_zero":
				return !fSharedInputValues.getBoolean("bi_elevator_has_been_zeroed");
			case "st_ball_collector_zero":
				return !fSharedInputValues.getBoolean("bi_ball_collector_has_been_zeroed");
			case "st_climb_zero":
				return !fSharedInputValues.getBoolean("bi_climber_has_been_zeroed");
			case "st_crossbow_zero":
				return !fSharedInputValues.getBoolean("bi_crossbow_has_been_zeroed");
		}

		// Check isReady on auto states
		// This reads the string assembled from the webdashboard and checks it against all possible autos until it finds a match
		// If it doesn't find a match it does nothing
		if (fSharedInputValues.getBoolean("bi_drive_has_been_zeroed") && fSharedInputValues.getBoolean("bi_odometry_has_been_zeroed")) {
			if (name.equals(fCombinedAuto)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isDone(String name, State state) {
		//Checks the isDone on zero states and determines when autonomous is done
		if((state.isDone()) || (fInterruptAutoButton && name.contains("auto"))){
			if(name.contains("auto")){
				fSharedInputValues.setBoolean("bi_auto_complete", true);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isRequestingDisposal(String name, State state) {

		// Only active states are called. state.isRequestingDisposal should return false for states using the interrupt system
		//      and true for states using the default system
		switch (name) {
			default:
				return state.isRequestingDisposal();
		}
	}

	public void dispose() {
	}
}
