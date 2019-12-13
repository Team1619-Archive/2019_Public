package org.team1619.models.inputs.numeric.sim;

import org.team1619.utilities.eventbus.Subscribe;
import org.team1619.events.sim.SimNumericInputSetEvent;
import org.team1619.shared.abstractions.EventBus;

public class SimNumericInputListener {

	private final Object fName;

	private double fValue = 0.0;

	public SimNumericInputListener(EventBus eventBus, Object name) {
		eventBus.register(this);
		fName = name;
	}

	public double get() {
		return fValue;
	}

	@Subscribe
	public void onNumericInputSet(SimNumericInputSetEvent event) {
		if (event.name.equals(fName)) {
			fValue = event.value;
		}
	}
}
