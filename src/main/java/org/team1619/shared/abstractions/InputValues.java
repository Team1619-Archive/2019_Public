package org.team1619.shared.abstractions;

import javax.annotation.Nullable;
import java.util.Map;

public interface InputValues {

	boolean getBoolean(String name, @Nullable Object flag);

	boolean getBooleanRisingEdge(String name);

	boolean getBoolean(String name);

	Map<String, Boolean> getAllBooleans();

	double getNumeric(String name, @Nullable Object flag);

	double getNumeric(String name);

	Map<String, Double> getAllNumerics();

	Map<String, Double> getVector(String name, @Nullable Object flag);

	Map<String, Double> getVector(String name);

	Map<String, Map<String, Double>> getAllVectors();

	String getString(String name);

	Map<String, String> getAllStrings();

	void setBoolean(String name, boolean value);

	void setBooleanRisingEdge(String name, boolean value);

	void setNumeric(String name, double value);

	void setVector(String name, Map<String, Double> values);

	void setString(String name, String value);

	Object getInputFlag(String name);
}
