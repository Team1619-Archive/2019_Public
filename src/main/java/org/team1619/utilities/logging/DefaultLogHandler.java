package org.team1619.utilities.logging;

/**
 * Prints messages to the console
 */

public class DefaultLogHandler implements LogHandler {

	@Override
	public void trace(String message) {
		System.out.println(message);
	}

	@Override
	public void debug(String message) {
		System.out.println(message);
	}

	@Override
	public void info(String message) {
		System.out.println(message);
	}

	@Override
	public void error(String message) {
		System.err.println(message);
	}
}
