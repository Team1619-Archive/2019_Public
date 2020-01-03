package org.team1619.utilities.logging;

/**
 * Handles the incoming messages from the log manager
 */

public interface LogHandler {

	void trace(String message);

	void debug(String message);

	void info(String message);

	void error(String message);
}
