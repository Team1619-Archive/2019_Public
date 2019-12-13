package org.team1619.models.inputs.vector.sim;

import org.team1619.models.inputs.vector.AccelerometerInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.utilities.Config;

import java.util.HashMap;
import java.util.Map;

public class SimAccelerationInput extends AccelerometerInput {

	private SimVectorInputListener fListener;

	public SimAccelerationInput(EventBus eventBus, Object name, Config config, InputValues inputValues) {
		super(name, config);

		fListener = new SimVectorInputListener(eventBus, name, new HashMap<>());
	}

	@Override
	public Map<String, Double> getAcceleration() {
		return fListener.get();
	}
}
