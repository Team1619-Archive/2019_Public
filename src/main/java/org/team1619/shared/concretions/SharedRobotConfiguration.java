package org.team1619.shared.concretions;

import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.models.exceptions.ConfigurationInvalidTypeException;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.YamlConfigParser;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

@Singleton
public class SharedRobotConfiguration implements RobotConfiguration {
	private static final Logger sLogger = LogManager.getLogger(SharedRobotConfiguration.class);

	private final Yaml fYaml = new Yaml();

	private Map<String, Map<String, Object>> fData = new HashMap<>();

	@Override
	public void initialize() {
		sLogger.debug("Loading robot-configuration.yaml file");

		YamlConfigParser parser = new YamlConfigParser();
		parser.loadWithFolderName("robot-configuration.yaml");
		fData = parser.getData();

		sLogger.debug("Loaded");
	}

	@Override
	public List<String> getStateNames() {
		return getList("general", "states");
	}

	@Override
	public Set<String> getSubsystemNames() {
		return getSet("general", "subsystems");
	}

	@Override
	public Set<String> getBooleanInputNames() {
		return getSet("general", "boolean_inputs");
	}

	@Override
	public Set<String> getNumericInputNames() {
		return getSet("general", "numeric_inputs");
	}

	@Override
	public Set<String> getVectorInputNames() {
		return getSet("general", "vector_inputs");
	}

	@Override
	public Set<String> getMotorNames() {
		return getSet("general", "motors");
	}

	@Override
	public Set<String> getSolenoidNames() {
		return getSet("general", "solenoids");
	}

	@Override
	public Object get(String category, String key) {
		ensureExists(category, key);
		return fData.get(category).get(key);
	}

	@Override
	public Map<String, Object> getCategory(String category) {
		ensureCategoryExists(category);
		return fData.get(category);
	}

	@Override
	public int getInt(String category, String key) {
		ensureExists(category, key);
		try {
			return (int) fData.get(category).get(key);
		} catch (Exception ex) {
			if(fData.get(category).get(key) == null)
			{
				throw new ConfigurationInvalidTypeException("int", key, "null");
			}
			else {
				throw new ConfigurationInvalidTypeException("int", key, fData.get(category).get(key));
			}
		}
	}

	@Override
	public double getDouble(String category, String key) {
		ensureExists(category, key);
		try {
			return (double) fData.get(category).get(key);
		} catch (Exception ex) {
			if(fData.get(category).get(key) == null)
			{
				throw new ConfigurationInvalidTypeException("double", key, "null");
			}
			else {
				throw new ConfigurationInvalidTypeException("double", key, fData.get(category).get(key));
			}
		}
	}

	@Override
	public boolean getBoolean(String category, String key) {
		ensureExists(category, key);
		try {
			return (boolean) fData.get(category).get(key);
		} catch (Exception ex) {
			if(fData.get(category).get(key) == null)
			{
				throw new ConfigurationInvalidTypeException("Boolean", key, "null");
			}
			else {
				throw new ConfigurationInvalidTypeException("Boolean", key, fData.get(category).get(key));
			}
		}
	}

	@Override
	public String getString(String category, String key) {
		ensureExists(category, key);
		try {
			return (String) fData.get(category).get(key);
		} catch (Exception ex) {
			if(fData.get(category).get(key) == null)
			{
				throw new ConfigurationInvalidTypeException("String", key, "null");
			}
			else {
				throw new ConfigurationInvalidTypeException("String", key, fData.get(category).get(key));
			}
		}
	}

	@Override
	public List getList(String category, String key) {
		ensureExists(category, key);
		try {
			return (List) fData.get(category).get(key);
		} catch (Exception ex) {
			throw new ConfigurationInvalidTypeException("int", key, fData.get(key));
		}
	}

	@Override
	public Set getSet(String category, String key) {
		ensureExists(category, key);
		try {
			return new HashSet((List) fData.get(category).get(key));
		} catch (ClassCastException ex) {
			throw new ConfigurationInvalidTypeException("set", key, fData.get(category).get(key));
		}
	}

	@Override
	public <T extends Enum<T>> T getEnum(String category, String key, Class<T> enumClass) {
		String value = getString(category, key).toUpperCase();

		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException ex) {
			throw new ConfigurationInvalidTypeException("enum", key, value);
		}
	}

	protected ClassLoader getClassLoader() {
		return YamlConfigParser.class.getClassLoader();
	}

	private void ensureExists(String category, String key) {
		ensureCategoryExists(category);
		if (!fData.get(category).containsKey(key)) {
			throw new ConfigurationException("***** No value found for key  '" + key + "' in category '" + category + "' *****");
		}
	}

	private void ensureCategoryExists(String category) {
		if (!fData.containsKey(category)) {
			throw new ConfigurationException("***** No category '" + category + "' found in SharedRobotConfiguration *****");
		}
	}

	public String toString() {
		return fData.toString();
	}
}
