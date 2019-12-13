package org.team1619.robot.competitionbot.state;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.state.State;
import org.team1619.robot.TeleopStateControls;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.Timer;

/**
 * determines which states to run
 */

public class Comp_TeleopStateControls extends TeleopStateControls {

	private static final Logger sLogger = LogManager.getLogger(Comp_TeleopStateControls.class);
	private Comp_ControllerLogic fCompControllerLogic;
	private Comp_ModeLogic fCompModeLogic;
	private Dashboard fDashboard;

	private boolean fOperatorY;
	private boolean fOperatorB;
	private boolean fOperatorA;
	private boolean fOperatorX;
	private boolean fOperatorRightTrigger;
	private boolean fOperatorRightBumper;
	private boolean fOperatorLeftTrigger;
	private boolean fOperatorDpadLeft;
	private boolean fOperatorDpadUp;
	private boolean fOperatorDpadRight;
	private boolean fOperatorDpadDown;
	private boolean fOperatorLeftBumper;
	private boolean fDriverDpadLeft;
	private boolean fUseTopLimelight;
	private boolean fTopLimelightHasTarget;
	private boolean fBottomLimelightHasTarget;
	private Comp_ModeLogic.ControllerMode fPreviousMode;
	private Timer fBallCollectorReZeroTimer;


	@Inject
	public Comp_TeleopStateControls(InputValues inputValues, RobotConfiguration robotConfiguration, Dashboard dashboard) {
		super(inputValues, robotConfiguration);
		fDashboard = dashboard;
		fCompControllerLogic = new Comp_ControllerLogic(inputValues, robotConfiguration);
		fCompModeLogic = new Comp_ModeLogic(inputValues, robotConfiguration);
		fPreviousMode = fCompModeLogic.getControllerMode();
		fBallCollectorReZeroTimer = new Timer();
	}

	@Override
	public void initialize() {
		if (!fSharedInputValues.getBoolean("bi_has_been_zeroed")) {
			fSharedInputValues.setBoolean("bi_elevator_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_climber_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_ball_collector_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_crossbow_has_been_zeroed", false);
			fSharedInputValues.setBoolean("bi_has_been_zeroed", true);
			fSharedInputValues.getVector("vi_navx", "zero");
		}
		fSharedInputValues.setBoolean("bi_drive_has_been_zeroed", false);
		fSharedInputValues.setBoolean("bi_is_top_limelight", true);
		fSharedInputValues.setNumeric("ni_top_limelight_offset", 0.0);
		fSharedInputValues.setNumeric("ni_bottom_limelight_offset", 0.0);
		fUseTopLimelight = true;
		fTopLimelightHasTarget = false;
		fBottomLimelightHasTarget = false;
		fCompModeLogic.initialize();
		fBallCollectorReZeroTimer.reset();
		readBooleanRisingEdgeButtonsSequenceMode();
		readBooleanRisingEdgeButtonsTestSequenceMode();
	}

	@Override
	public void update() {
		fCompModeLogic.update();

		if(fPreviousMode != fCompModeLogic.getControllerMode()){
			readBooleanRisingEdgeButtonsSequenceMode();
			readBooleanRisingEdgeButtonsTestSequenceMode();
			fPreviousMode = fCompModeLogic.getControllerMode();
		}

		if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.Sequence_Mode) {
			readBooleanRisingEdgeButtonsSequenceMode();
		} else if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.Test_Sequence_Mode) {
			readBooleanRisingEdgeButtonsTestSequenceMode();
		}

		if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.State_Mode) {
			fCompControllerLogic.update();
		}

		if(fSharedInputValues.getBoolean("bi_operator_dpad_up") && !fBallCollectorReZeroTimer.isStarted()){
			fBallCollectorReZeroTimer.start(1000);
		} else if (!fSharedInputValues.getBoolean("bi_operator_dpad_up")){
			fBallCollectorReZeroTimer.reset();
		} else if (fBallCollectorReZeroTimer.isDone()){
			fSharedInputValues.setBoolean("bi_ball_collector_has_been_zeroed", false);
			fBallCollectorReZeroTimer.reset();
		}


		// Only turn on Leds when we want to target
//		fDashboard.setNetworkTableValue("limelight-top", "camMode", 0);
//		fDashboard.setNetworkTableValue("limelight-bottom", "camMode", 0);
		fSharedInputValues.setBoolean("bi_is_top_limelight", !(fSharedInputValues.getNumeric("ni_elevator_primary_position") > 20.0 && fSharedInputValues.getNumeric("ni_elevator_primary_position") < 40.0));
		if (fSharedInputValues.getBoolean("bi_driver_left_bumper")) {
			if (fSharedInputValues.getBoolean("bi_is_top_limelight")) {
				fDashboard.setNetworkTableValue("limelight-top", "ledMode", 0);
				fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 1);
			} else {
				fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 0);
				fDashboard.setNetworkTableValue("limelight-top", "ledMode", 1);
			}
		} else {
			fDashboard.setNetworkTableValue("limelight-top", "ledMode", 1);
			fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 1);
		}

		//Sets values used to determine if LLDirect is ready
		fUseTopLimelight = fSharedInputValues.getBoolean("bi_is_top_limelight");
		fTopLimelightHasTarget = (fSharedInputValues.getVector("vi_limelight_top").get("tv") > 0);
		fBottomLimelightHasTarget = (fSharedInputValues.getVector("vi_limelight_bottom").get("tv") > 0);
	}

	// These are states that should be allowed to complete once they have started.
	// Only include states that cannot get stuck and never complete
	@Override
	public boolean doNotInterrupt(String name) {
		switch (name) {
			case "st_crossbow_extend":
				return true;
			case "st_crossbow_retract":
				return true;
		}
		return false;
	}

	// These states will always be active if no other states wants their subsystem
	@Override
	public boolean isDefaultState(String name) {
		switch (name) {
			case "st_drive_percent":
				return true;
			case "st_climb_idle":
				return true;
		}
		return false;
	}


	@Override
	public boolean isReady(String name) {

		if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.Sequence_Mode) {
			switch (name) {

				// ------- Drive -------
				case "st_drive_zero":
					return !fSharedInputValues.getBoolean("bi_drive_has_been_zeroed");
				case "st_drive_lldirect":
					return fSharedInputValues.getBoolean("bi_driver_left_bumper") && (fUseTopLimelight ? fTopLimelightHasTarget : fBottomLimelightHasTarget);
				case "st_drive_percent":
					return true;

				// ------- Elevator -------
				case "st_elevator_zero":
					return !fSharedInputValues.getBoolean("bi_elevator_has_been_zeroed");

				// ------- Crossbow -------
				case "st_crossbow_zero":
					return !fSharedInputValues.getBoolean("bi_crossbow_has_been_zeroed");


				// ------- Ball Collector -------
				case "st_ball_collector_zero":
					return !fSharedInputValues.getBoolean("bi_ball_collector_has_been_zeroed");

				// ------- climber -------
				case "st_climb_zero":
					return !fSharedInputValues.getBoolean("bi_climber_has_been_zeroed");
				case "st_climb_deploy":
					return fSharedInputValues.getBoolean("bi_driver_dpad_up") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_lock_on":
					return fSharedInputValues.getBoolean("bi_driver_dpad_right") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_climb":
					return fSharedInputValues.getBoolean("bi_driver_dpad_down") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_manual_override":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_start") && fSharedInputValues.getBoolean("bi_climb_enabled");
				case "st_climb_idle":
					return true;


				// ------- Sequences -------
				case "pl_protect":
					return fOperatorRightBumper;
				case "sq_hatch_collect":
					return fOperatorRightTrigger;
				case "sq_place_hatch_low":
					return fOperatorA && (fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_place_hatch_mid":
					return fOperatorB && (fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_place_hatch_high":
					return fOperatorY && (fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_ball_collect_floor":
					return fOperatorLeftTrigger;
				case "sq_place_ball_low":
					return fOperatorA && (!fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_place_ball_mid":
					return fOperatorB && (!fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_place_ball_high":
					return fOperatorY && (!fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_place_ball_cargoship":
					return fOperatorX && (!fSharedInputValues.getString("si_crossbow_position").equals("extended"));
				case "sq_climb_automated":
					return fDriverDpadLeft && fSharedInputValues.getBoolean("bi_climb_enabled");



				// ------- Undefined states -------
				default:
					return false;
			}
		} else if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.State_Mode) {

			switch (name) {

				// ------- Drive -------
				case "st_drive_zero":
					return !fSharedInputValues.getBoolean("bi_drive_has_been_zeroed");
				case "st_drive_lldirect":
					return fSharedInputValues.getBoolean("bi_driver_left_bumper") && (fUseTopLimelight ? fTopLimelightHasTarget : fBottomLimelightHasTarget);
				case "st_drive_percent":
					return true;
				case "st_drive_pure_pursuit":
					return false;
				case "st_drive_straight":
					return fSharedInputValues.getBoolean("bi_driver_a");

				// ------- Elevator -------
				case "st_elevator_zero":
					return !fSharedInputValues.getBoolean("bi_elevator_has_been_zeroed");
				case "st_elevator_cargoship":
					return fCompControllerLogic.getElevatorStateEnabled("cargoship");
				case "st_elevator_hatch_high":
					return fCompControllerLogic.getElevatorStateEnabled("hatch_high");
				case "st_elevator_hatch_mid":
					return fCompControllerLogic.getElevatorStateEnabled("hatch_mid");
				case "st_elevator_low":
					return fCompControllerLogic.getElevatorStateEnabled("low");
				case "st_elevator_ball_high":
					return fCompControllerLogic.getElevatorStateEnabled("ball_high");
				case "st_elevator_ball_mid":
					return fCompControllerLogic.getElevatorStateEnabled("ball_mid");
				case "st_elevator_protect":
					return fCompControllerLogic.getElevatorStateEnabled("protect");

				// ------- Crossbow -------
				case "st_crossbow_zero":
					return !fSharedInputValues.getBoolean("bi_crossbow_has_been_zeroed");
				case "st_crossbow_extend":
					return fCompControllerLogic.getCrossbowStateEnabled("extend");
				case "st_crossbow_retract":
					return fCompControllerLogic.getCrossbowStateEnabled("retract");


				// ------- Ball Collector -------
				case "st_ball_collector_zero":
					return !fSharedInputValues.getBoolean("bi_ball_collector_has_been_zeroed");
				case "st_ball_collector_stow":
					return fCompControllerLogic.getBallCollectorStateEnabled("stow");
				case "st_ball_collector_place_rocket":
					return fCompControllerLogic.getBallCollectorStateEnabled("place_rocket");
				case "st_ball_collector_place_cargoship":
					return fCompControllerLogic.getBallCollectorStateEnabled("place_cargoship");
				case "st_ball_collector_collect_floor":
					return fCompControllerLogic.getBallCollectorStateEnabled("collect_floor");
				case "st_ball_collector_protect":
					return fCompControllerLogic.getBallCollectorStateEnabled("protect");

				// ------- climber -------
				case "st_climb_zero":
					return !fSharedInputValues.getBoolean("bi_climber_has_been_zeroed");
				case "sq_climb_automated":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_left") && fSharedInputValues.getBoolean("bi_climb_enabled");
				case "st_climb_deploy":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_up") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_lock_on":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_right") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_climb":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_down") && fSharedInputValues.getBoolean("bi_climb_enabled") && !fSharedInputValues.getBoolean("bi_driver_start");
				case "st_climb_manual_override":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_start") && fSharedInputValues.getBoolean("bi_climb_enabled");
				case "st_climb_idle":
					return true;
				// ------- Undefined states -------
				default:
					return false;
			}


		} else if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.Test_Sequence_Mode) {
			switch (name) {
				case "sq_crossbow_check":
					return fOperatorDpadLeft;
				case "sq_ball_arm_check":
					return fOperatorDpadUp;
				case "sq_elevator_check":
					return fOperatorDpadRight;
				case "sq_drivetrain_check":
					return fOperatorDpadDown;
				case "st_drive_percent":
					return true;
				case "sq_full_system_check":
					return fOperatorLeftBumper;
				case "sq_climb_automated":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_left") && fSharedInputValues.getBoolean("bi_climb_enabled");
				case "st_climb_manual_override":
					return fSharedInputValues.getBooleanRisingEdge("bi_driver_start") && fSharedInputValues.getBoolean("bi_climb_enabled");
				case "st_climb_idle":
					return true;
				case "pl_protect_all":
					return fOperatorRightBumper;


				// ------- Undefined states -------
				default:
					return false;
			}
		} else if (fCompModeLogic.getControllerMode() == Comp_ModeLogic.ControllerMode.Test_Manual_Mode) {
			switch (name) {
				case "st_drive_percent":
					return true;
				case "st_elevator_manual":
					return true;
				case "st_crossbow_manual":
					return true;
				case "st_ball_collector_manual":
					return true;
				case "st_climb_manual":
					return true;

				// ------- Undefined states -------
				default:
					return false;

			}
		}
		return false;
	}


	@Override
	public boolean isDone(String name, State state) {

		// Only active states are called. state.isDone should return true except for states that are waiting to complete something.
		switch (name) {
			case "sq_climb_automated":
				return (state.isDone() || (fSharedInputValues.getBoolean("bi_driver_start")));
			case "st_climb_deploy":
				return (!fSharedInputValues.getBoolean("bi_driver_dpad_up"));
			case "st_climb_lock_on":
				return (!fSharedInputValues.getBoolean("bi_driver_dpad_right"));
			case "st_climb_climb":
				return (!fSharedInputValues.getBoolean("bi_driver_dpad_down"));
			default:
				return state.isDone();
		}
	}

	@Override
	public boolean isRequestingDisposal(String name, State state) {

		// Only active states are called. state.isRequestingDisposal should return false for states using the interrupt system
		//      and true for states using the default system
		switch (name) {
			case "st_drive_lldirect":
				 return !fSharedInputValues.getBoolean("bi_driver_left_bumper");
			case "st_drive_straight":
				return !fSharedInputValues.getBoolean("bi_driver_a");
			case "st_climb_deploy":
				return !fSharedInputValues.getBoolean("bi_driver_dpad_up");
			case "st_climb_lock_on":
				return !fSharedInputValues.getBoolean("bi_driver_dpad_right");
			case "st_climb_climb":
				return !fSharedInputValues.getBoolean("bi_driver_dpad_down");
			case "st_climb_manual_override":
				return !fSharedInputValues.getBoolean("bi_driver_start");
			default:
				return state.isRequestingDisposal();
		}
	}

	private void readBooleanRisingEdgeButtonsSequenceMode() {
		//Read all booleanRisingEdge Buttons
		fOperatorY = fSharedInputValues.getBooleanRisingEdge("bi_operator_y");
		fOperatorB = fSharedInputValues.getBooleanRisingEdge("bi_operator_b");
		fOperatorA = fSharedInputValues.getBooleanRisingEdge("bi_operator_a");
		fOperatorX = fSharedInputValues.getBooleanRisingEdge("bi_operator_x");
		fOperatorRightTrigger = fSharedInputValues.getBooleanRisingEdge("bi_operator_right_trigger");
		fOperatorRightBumper = fSharedInputValues.getBooleanRisingEdge("bi_operator_right_bumper");
		fOperatorLeftTrigger = fSharedInputValues.getBooleanRisingEdge("bi_operator_left_trigger");
		fOperatorDpadUp = fSharedInputValues.getBooleanRisingEdge("bi_operator_dpad_up");
		fOperatorLeftBumper = fSharedInputValues.getBooleanRisingEdge("bi_operator_left_bumper");
		fDriverDpadLeft = fSharedInputValues.getBooleanRisingEdge("bi_driver_dpad_left");
	}

	private void readBooleanRisingEdgeButtonsTestSequenceMode() {
		//Read all booleanRisingEdge Buttons
		fOperatorRightBumper = fSharedInputValues.getBooleanRisingEdge("bi_operator_right_bumper");
		fOperatorDpadLeft = fSharedInputValues.getBooleanRisingEdge("bi_operator_dpad_left");
		fOperatorDpadUp = fSharedInputValues.getBooleanRisingEdge("bi_operator_dpad_up");
		fOperatorDpadRight = fSharedInputValues.getBooleanRisingEdge("bi_operator_dpad_right");
		fOperatorDpadDown = fSharedInputValues.getBooleanRisingEdge("bi_operator_dpad_down");
		fOperatorLeftBumper = fSharedInputValues.getBooleanRisingEdge("bi_operator_left_bumper");
	}

	@Override
	public void dispose() {
		fDashboard.setNetworkTableValue("limelight-bottom", "ledMode", 0);
		fDashboard.setNetworkTableValue("limelight-top", "ledMode", 0);
		fSharedInputValues.setBoolean("bi_vacuum_pump_enabled", false);
		sLogger.info("Limelight Top Offset {}", fSharedInputValues.getNumeric("ni_top_limelight_offset"));
		sLogger.info("Limelight Bottom Offset {}", fSharedInputValues.getNumeric("ni_bottom_limelight_offset"));
	}

}