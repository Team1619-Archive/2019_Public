package org.team1619.models.inputs.bool.sim;

import org.team1619.models.inputs.bool.DigitalInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.utilities.Config;


public class SimDigitalInput extends DigitalInput {
	private SimBooleanInputListener fListener;

	public SimDigitalInput(EventBus eventBus, Object name, Config config) {
		super(name, config);

		fListener = new SimBooleanInputListener(eventBus, name);
	}

	@Override
	public boolean getDigitalInputValue() {
		return fListener.get();
	}
}
