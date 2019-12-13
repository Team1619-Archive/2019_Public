package org.team1619.utilities.logging;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogManager {

	@Nullable
	private static LogManager fLogManager = null;

	private Level fCurrentLoggingLevel = Level.INFO;

	private DateTimeFormatter fDateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

	private LogManager() {

	}

	public static Logger getLogger(String prefix) {
		if (fLogManager == null) {
			fLogManager = new LogManager();
		}
		return new Logger(fLogManager, prefix);
	}

	public static Logger getLogger(Class prefix) {
		return getLogger(prefix.getSimpleName());
	}

	public void log(Level level, String prefix, String message, Object... args) {
		if (!shouldLog(level)) {
			return;
		}

		String line = LocalDateTime.now().format(fDateTimeFormatter) + " " + Thread.currentThread().getName() + " [" + level + "] " + prefix + " - " + buildMessage(message, args);

		if (level == Level.ERROR) {
			System.err.println(line);
		} else {
			System.out.println(line);
		}
	}

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
