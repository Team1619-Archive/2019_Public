package org.team1619.shared.concretions.sim;

import org.team1619.shared.abstractions.Dashboard;

public class SimDashboard implements Dashboard {

	@Override
	public void initialize(){
	}

	@Override
	public void putNumber(String name, double value) {

	}

	@Override
	public void putBoolean(String name, boolean value) {
	}

	@Override
	public double getNumber(String name) {
		return 0.0;
	}

	@Override
	public void putString(String key, String value) {

	}

	@Override
	public void setNetworkTableValue(String table, String Entry, Object value) {

	}

	@Override
	public void smartdashboardSetAuto() {

	}
    @Override
	public boolean autoSelectionRisingEdge (){
		return false;
	}
}
