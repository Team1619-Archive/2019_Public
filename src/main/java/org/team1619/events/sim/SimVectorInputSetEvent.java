package org.team1619.events.sim;

import java.util.Map;

public class SimVectorInputSetEvent {

	public final String name;
	public final Map<String, Double> values;

	public SimVectorInputSetEvent(String name, Map<String, Double> values) {
		this.name = name;
		this.values = values;
	}
}
