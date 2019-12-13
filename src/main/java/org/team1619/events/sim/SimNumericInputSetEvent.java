package org.team1619.events.sim;

public class SimNumericInputSetEvent {

	public final String name;
	public final double value;

	public SimNumericInputSetEvent(String name, double value) {
		this.name = name;
		this.value = value;
	}
}
