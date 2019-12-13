package org.team1619.robot.competitionbot.state;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Timer;

public class Comp_ModeLogic {
	private static final Logger sLogger = LogManager.getLogger(Comp_ModeLogic.class);
	private InputValues fSharedInputValues;

	// Modes
	private String fOperatorBackButton;
	private String fOperatorStartButton;
	private boolean fTestModeButtonsPrevious;
	private boolean fIsManualMode;
	private boolean fIsTestMode;
	private Timer fTimerStateMode;
	private Timer fTimerTestMode;
	private boolean fShouldPrint;

	public enum ControllerMode {
		Sequence_Mode,
		State_Mode,
		Test_Sequence_Mode,
		Test_Manual_Mode
	}

	ControllerMode fControllerMode;

	//Climb
	private Timer fTimerClimber;

	public Comp_ModeLogic(InputValues sharedInputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = sharedInputValues;

		//Modes
		fOperatorBackButton = "bi_operator_back";
		fOperatorStartButton = "bi_operator_start";
		fTimerStateMode = new Timer();
		fTimerTestMode = new Timer();

		//Climb
		fTimerClimber = new Timer();
		fControllerMode = ControllerMode.Sequence_Mode;
	}

	public void initialize(){
		fIsManualMode = false;
		fIsTestMode = false;
		fControllerMode = ControllerMode.Sequence_Mode;
		fSharedInputValues.setBoolean("bi_climb_enabled", false);
		fTestModeButtonsPrevious = false;
		fShouldPrint = true;
	}
	public void update() {
		setControllerMode();
		setClimbEnable();
	}

	public ControllerMode getControllerMode() {
		return fControllerMode;
	}

	// Set the mode that the controller operates in (Sequence, State, Test_Sequence, Test_Manual)
	private void setControllerMode() {
		boolean testModeButtons = fSharedInputValues.getBoolean(fOperatorStartButton, null) && fSharedInputValues.getBoolean(fOperatorBackButton, null);

		if (!fTimerStateMode.isStarted() && fSharedInputValues.getBooleanRisingEdge(fOperatorBackButton) && !fSharedInputValues.getBoolean(fOperatorStartButton, null)) {
			fTimerStateMode.start(1000);
		} else if (fTimerStateMode.isDone()) {
			fIsManualMode = !fIsManualMode;
			fTimerStateMode.reset();
			fShouldPrint = true;
		} else if (!fSharedInputValues.getBoolean(fOperatorBackButton, null) || fSharedInputValues.getBoolean(fOperatorStartButton, null)) {
			fTimerStateMode.reset();
		}

		if (!fTimerTestMode.isStarted() && (testModeButtons && (testModeButtons != fTestModeButtonsPrevious))) {
			fTimerTestMode.start(1000);
		} else if (fTimerTestMode.isDone()) {
			fIsTestMode = !fIsTestMode;
			fTimerTestMode.reset();
			fShouldPrint = true;
		} else if (!testModeButtons) {
			fTimerTestMode.reset();
		}

		if (fIsManualMode && fIsTestMode) {
			fControllerMode = ControllerMode.Test_Manual_Mode;
		} else if (!fIsManualMode && fIsTestMode) {
			fControllerMode = ControllerMode.Test_Sequence_Mode;
		} else if (fIsManualMode) {
			fControllerMode = ControllerMode.State_Mode;
		} else {
			fControllerMode = ControllerMode.Sequence_Mode;
		}

		if(fShouldPrint){
			sLogger.info("***** {} ******", fControllerMode.toString().toUpperCase());
			fShouldPrint = false;
		}
		fSharedInputValues.setString("si_mode", fControllerMode.toString() );
		fTestModeButtonsPrevious = testModeButtons;
	}

	//Enable the climb
	private void setClimbEnable() {
		if (!fTimerClimber.isStarted() && fSharedInputValues.getBooleanRisingEdge(fOperatorStartButton) && !fSharedInputValues.getBoolean(fOperatorBackButton, null)) {
			fTimerClimber.start(1000);
		} else if (fTimerClimber.isDone()) {
			sLogger.info("***** CLIMB ENABLED ******");
			fSharedInputValues.setBoolean("bi_climb_enabled", true);
			fTimerClimber.reset();
		} else if (!fSharedInputValues.getBoolean(fOperatorStartButton, null) || fSharedInputValues.getBoolean(fOperatorBackButton, null)) {
			fTimerClimber.reset();
		}
	}

}
