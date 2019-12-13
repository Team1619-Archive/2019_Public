package org.team1619.robot.protobot1.state;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.state.State;
import org.team1619.robot.AutoStateControls;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

/**
 * Determines which auto to run
 */

public class Pro1_AutoStateControls extends AutoStateControls {

	private static final Logger sLogger = LogManager.getLogger(Pro1_AutoStateControls.class);
	private Dashboard fDashboard;

	private String fStartingPosition;
	private String fAutoDestination;
	private boolean fInterruptAutoButton;
	private String fCombinedAuto;


	@Inject
	public Pro1_AutoStateControls(InputValues inputValues, RobotConfiguration robotConfiguration, Dashboard dashboard) {
		super(inputValues, robotConfiguration);
		fDashboard = dashboard;
		fStartingPosition = "none";
		fAutoDestination = "none";
		fCombinedAuto = "none";
	}

	@Override
	public void initialize() {
		fStartingPosition = (fSharedInputValues.getString("si_auto_position").toLowerCase().replaceAll("\\s","") );
		fAutoDestination = (fSharedInputValues.getString("si_auto").toLowerCase().replaceAll("\\s", ""));
		fCombinedAuto = "sq_auto_" + fStartingPosition + "_" + fAutoDestination;
		sLogger.info(fCombinedAuto);

		fSharedInputValues.setBoolean("bi_auto_complete", false);

		if (!fSharedInputValues.getBoolean("bi_has_been_zeroed")) {
			fSharedInputValues.setBoolean("bi_has_been_zeroed", true);
		}
		fInterruptAutoButton = false;
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
		}
	}

	@Override
	public boolean isReady(String name) {
		switch (name) {
		//	case "st_example":
			//	return true;
		}

		return name.equals(fCombinedAuto);
	}


	@Override
	public boolean isDone(String name, State state) {
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