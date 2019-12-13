package org.team1619.models.inputs.vector;

import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class Limelight extends VectorInput {

	private Map<String, Double> fValues = new HashMap<>();

	public Limelight(Object name, Config config) {
		super(name, config);
	}

	@Override
	public void update() {
		fValues = getData();
	}

	@Override
	public Map<String, Double> get(@Nullable Object flag) {
		return fValues;
	}

	public abstract Map<String, Double> getData();
}
