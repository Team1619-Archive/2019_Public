package org.team1619.models.inputs.numeric.sim;

import org.team1619.models.inputs.numeric.AxisInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.utilities.Config;

public class SimAxisInput extends AxisInput {

	private SimNumericInputListener fListener;

	public SimAxisInput(EventBus eventBus, Object name, Config config) {
		super(name, config);

		fListener = new SimNumericInputListener(eventBus, name);
	}

	@Override
	public double getAxis() {
		return fListener.get();
	}
}
