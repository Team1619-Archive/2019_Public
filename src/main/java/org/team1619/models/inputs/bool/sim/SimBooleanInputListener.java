package org.team1619.models.inputs.bool.sim;

import org.team1619.utilities.eventbus.Subscribe;
import org.team1619.events.sim.SimBooleanInputSetEvent;
import org.team1619.shared.abstractions.EventBus;

public class SimBooleanInputListener {

	private final Object fName;

	private boolean fValue = false;

	public SimBooleanInputListener(EventBus eventBus, Object name) {
		fName = name;
		eventBus.register(this);
	}

	public boolean get() {
		return fValue;
	}

	@Subscribe
	public void onBooleanInputSet(SimBooleanInputSetEvent event) {
		if (event.name.equals(fName)) {
			fValue = event.value;
		}
	}
}
