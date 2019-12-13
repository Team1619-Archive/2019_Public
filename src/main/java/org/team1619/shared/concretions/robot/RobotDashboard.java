package org.team1619.shared.concretions.robot;

import org.team1619.utilities.injection.Inject;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;


public class RobotDashboard implements Dashboard {

	private InputValues fSharedInputValues;
	Preferences prefs = Preferences.getInstance();
	SendableChooser fAutoOrigin = new SendableChooser();
	SendableChooser fAutoDestination = new SendableChooser();
	SendableChooser fAutoAction = new SendableChooser();
	private String fPreviousAutoOrigin = "none";
	private String fPreviousAutoDestination = "none";
	private String fPreviousAutoAction = "none";


	@Inject
	public RobotDashboard(InputValues inputValues) {
		fSharedInputValues = inputValues;
	}

	@Override
	public void initialize(){
		fAutoOrigin.addDefault("None", "None");
		fAutoOrigin.addObject("Left", "Left");
		fAutoOrigin.addObject("Right", "Right");
		fAutoOrigin.addObject("Center", "Center");
		SmartDashboard.putData("Origin", fAutoOrigin);
		fAutoDestination.addDefault("None", "None");
		fAutoDestination.addObject("Rocket Left", "Rocket Left");
		fAutoDestination.addObject("Cargo Ship Left", "Cargo Ship Left");
		fAutoDestination.addObject("Cargo Ship Right", "Cargo Ship Right");
		fAutoDestination.addObject("Rocket Right", "Rocket Right");
		SmartDashboard.putData("Action", fAutoAction);
		fAutoAction.addDefault("None", "None");
		fAutoAction.addObject("Front Low", "Front Low");
		fAutoAction.addObject("Back Low", "Back Low");
		fAutoAction.addObject("Double Low", "Double Low");
		fAutoAction.addObject("Double Mid", "Double Mid");
		SmartDashboard.putData("Destination", fAutoDestination);
		fPreviousAutoOrigin = fAutoOrigin.getSelected().toString();
		fPreviousAutoDestination = fAutoDestination.getSelected().toString();
		fPreviousAutoAction = fAutoAction.getSelected().toString();
	}

	@Override
	public void putNumber(String name, double value) {
		SmartDashboard.putNumber(name, value);
	}

	@Override
	public void putBoolean(String name, boolean value) {
		SmartDashboard.putBoolean(name, value);
	}

	//Intended for Preferences
	@Override
	public double getNumber(String name) {
		return prefs.getDouble(name, -1);
	}

	@Override
	public void putString(String key, String value) {
		SmartDashboard.putString(key, value);
	}

	@Override
	public void setNetworkTableValue(String table, String entry, Object value) {
		NetworkTableInstance.getDefault().getTable(table).getEntry(entry).setValue(value);
	}

	@Override
	public void smartdashboardSetAuto(){
		fSharedInputValues.setString("si_auto_origin", fAutoOrigin.getSelected().toString());
		fSharedInputValues.setString("si_auto_destination", fAutoDestination.getSelected().toString());
		fSharedInputValues.setString("si_auto_action", fAutoAction.getSelected().toString());
		fSharedInputValues.setString("si_selected_auto",
				fSharedInputValues.getString("si_auto_origin") + " to " +
						fSharedInputValues.getString("si_auto_destination") + ", " +
						fSharedInputValues.getString("si_auto_action"));
	}

	@Override
	public boolean autoSelectionRisingEdge (){
		String origin = fAutoOrigin.getSelected().toString();
		String destination = fAutoDestination.getSelected().toString();
		String action = fAutoAction.getSelected().toString();
		return (!origin.equals(fPreviousAutoOrigin) || !destination.equals(fPreviousAutoDestination) || !action.equals(fPreviousAutoAction));
	}

}
