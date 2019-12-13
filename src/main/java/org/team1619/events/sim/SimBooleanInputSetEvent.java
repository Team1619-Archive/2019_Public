package org.team1619.events.sim;

public class SimBooleanInputSetEvent {

	public final String name;
	public final boolean value;

	public SimBooleanInputSetEvent(String name, boolean value) {
		this.name = name;
		this.value = value;
	}
}
