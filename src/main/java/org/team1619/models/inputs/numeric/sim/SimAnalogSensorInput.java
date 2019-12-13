package org.team1619.models.inputs.numeric.sim;

import org.team1619.models.inputs.numeric.AnalogSensorInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.utilities.Config;

public class SimAnalogSensorInput extends AnalogSensorInput {

	private SimNumericInputListener fListener;

	public SimAnalogSensorInput(EventBus eventBus, Object name, Config config) {
		super(name, config);

		fListener = new SimNumericInputListener(eventBus, name);
	}

	@Override
	public double getVoltage() {
		return fListener.get();
	}

	public double getAccumulatorCount() {
		return fListener.get();
	}

	public double getAccumulatorValue() {
		return fListener.get();
	}

	public double getValue() {
		return fListener.get();
	}
}
