package org.team1619.shared.abstractions;

public interface Dashboard {

	void initialize();

	void putNumber(String name, double value);

	void putBoolean(String name, boolean value);

	double getNumber(String name);

	void putString(String key, String value);

	void setNetworkTableValue(String table, String Entry, Object value);

	void smartdashboardSetAuto();

	boolean autoSelectionRisingEdge ();
}
