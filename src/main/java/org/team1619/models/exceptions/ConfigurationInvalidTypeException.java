package org.team1619.models.exceptions;

public class ConfigurationInvalidTypeException extends ConfigurationException {
	public ConfigurationInvalidTypeException(String type, String key, Object data) {
		super("******** Expected " + type + " for " + key + " found " + data.getClass().getSimpleName() + " ********");
	}

	public ConfigurationInvalidTypeException(String type, String key, String found) {
		super("******** Expected " + type + " for " + key + " found " + found + " ********");
	}
}
