package org.team1619.robot.competitionbot.state;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Timer;

public class Comp_ControllerLogic {
	private static final Logger sLogger = LogManager.getLogger(Comp_ControllerLogic.class);
	private InputValues fSharedInputValues;

	//Elevator
	private String fElevatorState;
	private boolean fElevatorStateChanged;
	private String fElevatorHighButton;
	private String fElevatorMidButton;
	private String fElevatorCargoShipButton;
	private String fElevatorLowButton;

	//Crossbow
	private String fCrossbowState;
	private String fCrossbowExtendButton;
	private boolean fCrossbowStateChanged;

	//ball collector
	private boolean fBallCollectorStateChanged;
	private String fBallCollectorState;
	private String fBallCollectorCollectButton;
	private String fBallCollectorPlaceRocketButton;
	private String fBallCollectorCargoshipButton;
	private int ELEVATOR_HIGH_BALL_COLLECTOR_WAIT_TIME;
	private int ELEVATOR_MID_BALL_COLLECTOR_WAIT_TIME;
	private int ELEVATOR_CARGOSHIP_BALL_COLLECTOR_WAIT_TIME;

	//Protect
	private String fProtectButton;

	private Timer fTimerBallCollector = new Timer();

	public Comp_ControllerLogic(InputValues sharedInputValues, RobotConfiguration robotConfiguration) {
		fSharedInputValues = sharedInputValues;

		//Elevator
		fElevatorState = "low";
		fElevatorStateChanged = false;
		fElevatorHighButton = "bi_operator_y";
		fElevatorMidButton = "bi_operator_b";
		fElevatorCargoShipButton = "bi_operator_x";
		fElevatorLowButton = "bi_operator_a";

		//Crossbow
		fCrossbowState = "retract";
		fCrossbowExtendButton = "bi_operator_right_trigger";
		fCrossbowStateChanged = false;

		//Ball collector
		fBallCollectorCollectButton = "bi_operator_left_trigger";
		fBallCollectorPlaceRocketButton = "bi_operator_left_bumper";
		fBallCollectorCargoshipButton = "bi_operator_x";
		fBallCollectorState = "stow";
		fBallCollectorStateChanged = false;
		fProtectButton = "bi_operator_right_bumper";
		ELEVATOR_HIGH_BALL_COLLECTOR_WAIT_TIME = 900;
		ELEVATOR_MID_BALL_COLLECTOR_WAIT_TIME = 400;
		ELEVATOR_CARGOSHIP_BALL_COLLECTOR_WAIT_TIME = 300;
	}

	public void update() {
		setElevatorMode();
		setCrossbowMode();
		setBallCollectorMode();
		setProtected();
	}

	public boolean getElevatorStateEnabled(String state) {
		if (fElevatorStateChanged && fElevatorState.equals(state)) {
			fElevatorStateChanged = false;
			return true;
		} else {
			return false;
		}
	}

	public boolean getCrossbowStateEnabled(String state) {
		if (fCrossbowStateChanged && fCrossbowState.equals(state)) {
			fCrossbowStateChanged = false;
			return true;
		} else {
			return false;
		}
	}

	public boolean getBallCollectorStateEnabled(String state) {
		if (fBallCollectorStateChanged && fBallCollectorState.equals(state)) {
			fBallCollectorStateChanged = false;
			return true;
		} else {
			return false;
		}
	}

	// Set the Elevator's position
	private void setElevatorMode() {
		//	boolean elevatorForceHatchButton = fSharedInputValues.getBoolean(fElevatorForceHatchButton);
		//	boolean ballCollectorBeamSensor = fSharedInputValues.getBoolean(fBallCollectorBeamSensor);
		boolean isCrossbowExtended = fCrossbowState.equals("extend");
		boolean shouldPrint = false;
		if (fSharedInputValues.getBooleanRisingEdge(fElevatorHighButton)) {
			//	if (!ballCollectorBeamSensor || elevatorForceHatchButton) {
			if (isCrossbowExtended) {
				fElevatorState = "hatch_high";
				//fCrossbowState = "extend";
				//fBallCollectorState = "stow";
				fElevatorStateChanged = true;
				//fCrossbowStateChanged = true;
				//	fBallCollectorStateChanged = true;
				shouldPrint = true;
			} else {
				fElevatorState = "ball_high";
				//fCrossbowState = "retract";
				fBallCollectorState = "place_rocket";
				fElevatorStateChanged = true;
				//	fCrossbowStateChanged = true;
				fTimerBallCollector.reset();
				fTimerBallCollector.start(ELEVATOR_HIGH_BALL_COLLECTOR_WAIT_TIME);
//				fBallCollectorStateChanged = true;
				shouldPrint = true;
			}
		} else if (fSharedInputValues.getBooleanRisingEdge(fElevatorMidButton)) {
			//	if (!ballCollectorBeamSensor || elevatorForceHatchButton) {
			if (isCrossbowExtended) {
				fElevatorState = "hatch_mid";
				//fCrossbowState = "extend";
				//	fBallCollectorState = "stow";
				fElevatorStateChanged = true;
				//	fCrossbowStateChanged = true;
				//	fBallCollectorStateChanged = true;
				shouldPrint = true;
			} else {
				fElevatorState = "ball_mid";
				//	fCrossbowState = "retract";
				fBallCollectorState = "place_rocket";
				fElevatorStateChanged = true;
				//	fCrossbowStateChanged = true;
				//		fBallCollectorStateChanged = true;
				fTimerBallCollector.reset();
				fTimerBallCollector.start(ELEVATOR_MID_BALL_COLLECTOR_WAIT_TIME);
				shouldPrint = true;
			}
		} else if (fSharedInputValues.getBooleanRisingEdge(fElevatorCargoShipButton)) {
			//if(ballCollectorBeamSensor) {
			fElevatorState = "cargoship";
			fCrossbowState = "retract";
			fBallCollectorState = "place_cargoship";
			fElevatorStateChanged = true;
			fCrossbowStateChanged = true;
			fTimerBallCollector.reset();
			fTimerBallCollector.start(ELEVATOR_CARGOSHIP_BALL_COLLECTOR_WAIT_TIME);
//			fBallCollectorStateChanged = true;
			shouldPrint = true;
			//	}
		} else if (fSharedInputValues.getBooleanRisingEdge(fElevatorLowButton)) {
			//	if (!ballCollectorBeamSensor || elevatorForceHatchButton) {
			if (isCrossbowExtended) {
				fElevatorState = "low";
				//	fCrossbowState = "extend";
				//	fBallCollectorState = "stow";
				fElevatorStateChanged = true;
				//fCrossbowStateChanged = true;
				//	fBallCollectorStateChanged = true;
				shouldPrint = true;
			} else {
				fElevatorState = "low";
				//	fCrossbowState = "retract";
				fBallCollectorState = "place_rocket";
				fElevatorStateChanged = true;
				//	fCrossbowStateChanged = true;
				fBallCollectorStateChanged = true;
				shouldPrint = true;
			}
		}

		if (shouldPrint) {
			sLogger.debug("Elevator State = {}", fElevatorState);
		}

		if (fTimerBallCollector.isDone()) {
			fBallCollectorStateChanged = true;
			fTimerBallCollector.reset();
		}
	}

	//Set the crossbow's position
	private void setCrossbowMode() {
		boolean shouldPrint = false;
		if (fSharedInputValues.getBooleanRisingEdge(fCrossbowExtendButton)) {
			fCrossbowState = fCrossbowState.equals("retract") ? "extend" : "retract";
			fCrossbowStateChanged = true;
			fBallCollectorState = fCrossbowState.equals("extend") ? "stow" : fBallCollectorState;
			fBallCollectorStateChanged = true;
			shouldPrint = true;
		}

		if (shouldPrint) {
			sLogger.debug("Extended = {},", fCrossbowState);
		}
	}

	// Set the Ball collector's position
	private void setBallCollectorMode() {
		boolean shouldPrint = false;
		if (fSharedInputValues.getBooleanRisingEdge(fBallCollectorCollectButton)) {
			fBallCollectorState = fBallCollectorState.equals("stow") || fBallCollectorState.equals("protect") ? "collect_floor" : "stow";
			shouldPrint = true;
			fBallCollectorStateChanged = true;
			fCrossbowState = !fBallCollectorState.equals("stow") ? "retract" : fCrossbowState;
			fCrossbowStateChanged = true;
		} else if (fSharedInputValues.getBooleanRisingEdge(fBallCollectorPlaceRocketButton)) {
			fBallCollectorState = "place_rocket";
			shouldPrint = true;
			fBallCollectorStateChanged = true;
			fCrossbowState = "retract";
			fCrossbowStateChanged = true;
		} else if (fSharedInputValues.getBooleanRisingEdge(fBallCollectorCargoshipButton)) {
			fBallCollectorState = "place_cargoship";
			shouldPrint = true;
			fBallCollectorStateChanged = true;
			fCrossbowState = "retract";
			fCrossbowStateChanged = true;
		}

		if (shouldPrint) {
			sLogger.debug("Ball Collector State = {}", fBallCollectorState);
		}
	}

	//Set ball collector and crossbow to a protected state
	private void setProtected() {
		boolean shouldPrint = false;
		if (fSharedInputValues.getBooleanRisingEdge(fProtectButton)) {
			fBallCollectorState = "protect";
			fBallCollectorStateChanged = true;
			fCrossbowState = "retract";
			fCrossbowStateChanged = true;
			fElevatorState = "protect";
			fElevatorStateChanged = true;
			shouldPrint = true;
		}

		if (shouldPrint) {
			sLogger.debug("Protect");
		}
	}

}
