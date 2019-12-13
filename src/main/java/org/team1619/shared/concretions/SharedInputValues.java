package org.team1619.shared.concretions;

import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedInputValues implements InputValues {

	private static final Logger sLogger = LogManager.getLogger(SharedInputValues.class);

	private Map<String, Boolean> fBooleanInputs = new ConcurrentHashMap<>();
	private Map<String, Boolean> fBooleanRisingEdgeInputs = new ConcurrentHashMap<>();
	private Map<String, Double> fNumericInputs = new ConcurrentHashMap<>();
	private Map<String, Map<String, Double>> fVectorInputs = new ConcurrentHashMap<>();
	private Map<String, String> fStringInputs = new ConcurrentHashMap<>();

	private Map<String, Object> fInputFlags = new ConcurrentHashMap<>();

	public boolean getBoolean(String name, @Nullable Object flag) {
		if (flag != null) {
			fInputFlags.put(name, flag);
		}

		return fBooleanInputs.getOrDefault(name, false);
	}

	public boolean getBoolean(String name) {
		return getBoolean(name, null);
	}

	public boolean getBooleanRisingEdge(String name) {
		boolean risingEdge = fBooleanRisingEdgeInputs.getOrDefault(name, false);
		fBooleanRisingEdgeInputs.put(name, false);
		return risingEdge;
	}

	@Override
	public Map<String, Boolean> getAllBooleans() {
		return fBooleanInputs;
	}

	public double getNumeric(String name, @Nullable Object flag) {
		if (flag != null) {
			fInputFlags.put(name, flag);
		}
		return fNumericInputs.getOrDefault(name, 0.0);
	}

	public double getNumeric(String name) {
		return getNumeric(name, null);
	}

	@Override
	public Map<String, Double> getAllNumerics() {
		return fNumericInputs;
	}

	public Map<String, Double> getVector(String name, @Nullable Object flag) {
		if (flag != null) {
			fInputFlags.put(name, flag);
		}
		return fVectorInputs.getOrDefault(name, new HashMap<>());
	}

	public Map<String, Double> getVector(String name) {
		return getVector(name, null);
	}


	@Override
	public Map<String, Map<String, Double>> getAllVectors() {
		return fVectorInputs;
	}

	@Override
	public String getString(String name) {
		return fStringInputs.getOrDefault(name, "DoesNotExist");
	}

	@Override
	public Map<String, String> getAllStrings() {
		return fStringInputs;
	}

	public void setBoolean(String name, boolean value) {
		//sLogger.debug("Setting boolean input '{}' to {}", name, value);

		fBooleanInputs.put(name, value);
	}

	public void setBooleanRisingEdge(String name, boolean value) {
		//sLogger.debug("Setting boolean rising edge input '{}' to {}", name, value);
		fBooleanRisingEdgeInputs.put(name, value);
	}

	@Override
	public void setNumeric(String name, double value) {
		//sLogger.debug("Setting numeric input '{}' to {}", name, value);

		fNumericInputs.put(name, value);
	}

	@Override
	public void setVector(String name, Map<String, Double> values) {
		//sLogger.debug("Setting vector {}", name);

		fVectorInputs.put(name, values);
	}

	@Override
	public void setString(String name, String value) {
		fStringInputs.put(name, value);
	}

	@Override
	public Object getInputFlag(String name) {
		Object flag = fInputFlags.getOrDefault(name, null);
		fInputFlags.remove(name);
		return flag;
	}
}
