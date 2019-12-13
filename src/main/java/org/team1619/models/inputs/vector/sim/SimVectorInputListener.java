package org.team1619.models.inputs.vector.sim;

import org.team1619.utilities.eventbus.Subscribe;
import org.team1619.events.sim.SimVectorInputSetEvent;
import org.team1619.shared.abstractions.EventBus;

import java.util.Map;

public class SimVectorInputListener {

	private final Object fName;
	private Map<String, Double> fValues;

	public SimVectorInputListener(EventBus eventBus, Object name, Map<String, Double> startingValues) {
		eventBus.register(this);
		fName = name;
		fValues = startingValues;
	}

	public Map<String, Double> get() {
		return fValues;
	}

	@Subscribe
	public void onNumericInputSet(SimVectorInputSetEvent event) {
		if (event.name.equals(fName)) {
			fValues = event.values;
		}
	}
}
