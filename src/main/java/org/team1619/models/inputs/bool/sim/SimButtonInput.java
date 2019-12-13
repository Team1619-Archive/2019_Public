package org.team1619.models.inputs.bool.sim;

import org.team1619.models.inputs.bool.ButtonInput;
import org.team1619.shared.abstractions.EventBus;
import org.team1619.utilities.Config;

public class SimButtonInput extends ButtonInput {

	private SimBooleanInputListener fListener;

	public SimButtonInput(EventBus eventBus, Object name, Config config) {
		super(name, config);

		fListener = new SimBooleanInputListener(eventBus, name);
	}

	@Override
	public boolean isPressed() {
		return fListener.get();
	}
}
