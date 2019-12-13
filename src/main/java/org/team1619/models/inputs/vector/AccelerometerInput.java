package org.team1619.models.inputs.vector;

import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public abstract class AccelerometerInput extends VectorInput {

	private Map<String, Double> fAcceleration = new HashMap<>();

	public AccelerometerInput(Object name, Config config) {
		super(name, config);
	}

	@Override
	public void update() {
		fAcceleration = getAcceleration();
	}

	@Override
	public void initialize() {

	}

	@Override
	public Map<String, Double> get(@Nullable Object flag) {
		return fAcceleration;
	}

	public abstract Map<String, Double> getAcceleration();
}
