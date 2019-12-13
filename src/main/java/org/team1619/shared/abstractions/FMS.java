package org.team1619.shared.abstractions;

public interface FMS {

	enum Mode {
		AUTONOMOUS,
		TELEOP,
		DISABLED,
		TEST
	}

	void setMode(Mode mode);

	Mode getMode();
}
