package org.team1619.utilities.logging;

public class Logger {

	private final LogManager fLogManager;
	private final String fPrefix;

	protected Logger(LogManager logManager, String prefix) {
		fLogManager = logManager;
		fPrefix = prefix;
	}

	public void trace(String message, Object... args) {
		fLogManager.log(LogManager.Level.TRACE, fPrefix, message, args);
	}

	public void debug(String message, Object... args) {
		fLogManager.log(LogManager.Level.DEBUG, fPrefix, message, args);
	}

	public void info(String message, Object... args) {
		fLogManager.log(LogManager.Level.INFO, fPrefix, message, args);
	}

	public void error(String message, Object... args) {
		fLogManager.log(LogManager.Level.ERROR, fPrefix, message, args);
	}

	public void error(Exception message, Object... args) {
		error(message.toString(), args);
	}

	public void log(LogManager.Level level, String message, Object... args) {
		fLogManager.log(level, fPrefix, message, args);
	}
}