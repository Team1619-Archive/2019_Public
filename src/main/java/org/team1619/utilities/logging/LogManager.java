package org.team1619.utilities.logging;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Takes the log messages from each logger and sends them to the log handlers
 */

public class LogManager {

	@Nullable
	private static LogManager fLogManager = null;

	private HashSet<LogHandler> logHandlers = new HashSet<>();

	private Level fCurrentLoggingLevel = Level.INFO;

	private DateTimeFormatter fDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	private LogManager() {

	}

	// Creates a logger and a log manager if necessary
	public static Logger getLogger(String prefix) {
		if (fLogManager == null) {
			fLogManager = new LogManager();
			addLogHandler(new DefaultLogHandler());
		}
		return new Logger(fLogManager, prefix);
	}


	public static Logger getLogger(Class prefix) {
		return getLogger(prefix.getSimpleName());
	}

	// Creates a log manager if necessary and adds the logHandler to the list of log handlers
	public static void addLogHandler(LogHandler logHandler) {
		if (fLogManager == null) {
			fLogManager = new LogManager();
			addLogHandler(new DefaultLogHandler());
		}
		fLogManager.logHandlers.add(logHandler);
	}

	public static void removeLogHandler(LogHandler logHandler) {
		if(fLogManager != null) {
			fLogManager.logHandlers.remove(logHandler);
		}
	}

	// Takes a log message and passes it to the log handler
	public void log(Level level, String prefix, String message, Object... args) {
		if (!shouldLog(level)) {
			return;
		}

		final String line = LocalDateTime.now().format(fDateTimeFormatter) + " " + Thread.currentThread().getName() + " [" + level + "] " + prefix + " - " + buildMessage(message, args);

		switch (level) {
			case TRACE:
				logHandlers.forEach((handler) -> handler.trace(line));
				break;
			case DEBUG:
				logHandlers.forEach((handler) -> handler.debug(line));
				break;
			case INFO:
				logHandlers.forEach((handler) -> handler.info(line));
				break;
			case ERROR:
				logHandlers.forEach((handler) -> handler.error(line));
				break;
		}
	}

	// Combines the different parts of the message
	private String buildMessage(String message, Object... args) {
		if (message.endsWith("{}")) {
			message += " ";
		}

		StringBuilder line = new StringBuilder();

		String[] parts = message.split("\\{}");

		for (int p = 0; p < parts.length - 1; p++) {
			line.append(parts[p]);
			if (p < args.length) {
				line.append(args[p]);
			}
		}

		line.append(parts[parts.length - 1]);

		return line.toString().trim();
	}

	// Decides if a message is above the level that the logging is set to
	private boolean shouldLog(Level level) {
		return fCurrentLoggingLevel.getPriority() <= level.getPriority();
	}

	public enum Level {
		TRACE(0),
		DEBUG(1),
		INFO(2),
		ERROR(3);

		private final int fPriority;

		Level(int priority) {
			fPriority = priority;
		}

		public int getPriority() {
			return fPriority;
		}
	}
}
