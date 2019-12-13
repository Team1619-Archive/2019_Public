package org.team1619.utilities;

import org.team1619.models.exceptions.ConfigurationException;
import org.team1619.models.exceptions.ConfigurationInvalidTypeException;

import java.util.List;
import java.util.Map;

public class Config {

	private String fType;
	private Map<String, Object> fData;

	public Config(String type, Map data) {
		this.fType = type;
		this.fData = data;
	}

	public String getType() {
		return fType;
	}

	public Map<String, Object> getData() {
		return fData;
	}

	public Object get(String key) {
		ensureExists(key);
		return fData.get(key);
	}

	public Object get(String key, Object defaultValue) {
		try {
			return fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			return defaultValue;
		}
	}

	public int getInt(String key) {
		ensureExists(key);
		try {
			return (int) fData.get(key);
		} catch (Exception ex) {
			if (fData.get(key) == null) {
				throw new ConfigurationInvalidTypeException("int", key, "null");
			} else {
				throw new ConfigurationInvalidTypeException("int", key, fData.get(key));
			}
		}
	}

	public int getInt(String key, int defaultValue) {
		try {
			if (fData.get(key) == null) {
				return defaultValue;
			}
			return (int) fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("int", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public double getDouble(String key) {
		ensureExists(key);
		try {
			return (double) fData.get(key);
		} catch (Exception ex) {
			if (fData.get(key) == null) {
				throw new ConfigurationInvalidTypeException("double", key, "null");
			} else {
				throw new ConfigurationInvalidTypeException("double", key, fData.get(key));
			}
		}
	}

	public double getDouble(String key, double defaultValue) {
		try {
			if (fData.get(key) == null) {
				return defaultValue;
			}
			return (double) fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("double", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public boolean getBoolean(String key) {
		ensureExists(key);
		try {
			return (boolean) fData.get(key);
		} catch (Exception ex) {
			if (fData.get(key) == null) {
				throw new ConfigurationInvalidTypeException("boolean", key, "null");
			} else {
				throw new ConfigurationInvalidTypeException("boolean", key, fData.get(key));
			}
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		try {
			if (fData.get(key) == null) {
				return defaultValue;
			}
			return (boolean) fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("boolean", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public String getString(String key) {
		ensureExists(key);
		try {
			return (String) fData.get(key);
		} catch (Exception ex) {
			if (fData.get(key) == null) {
				throw new ConfigurationInvalidTypeException("string", key, "null");
			} else {
				throw new ConfigurationInvalidTypeException("string", key, fData.get(key));
			}
		}
	}

	public String getString(String key, String defaultValue) {
		try {
			if (fData.get(key) == null) {
				return defaultValue;
			}
			return (String) fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("string", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public List getList(String key) {
		ensureExists(key);
		try {
			return (List) fData.get(key);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("list", key, fData.get(key));
			} catch (Exception e) {
				throw new ConfigurationInvalidTypeException("list", key, "null");
			}
		}
	}

	public List getList(String key, List defaultValue) {
		try {
			if (fData.get(key) == null) {
				return defaultValue;
			}
			return (List) fData.getOrDefault(key, defaultValue);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("list", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass) {
		String value = getString(key).toUpperCase();

		try {
			return Enum.valueOf(enumClass, value);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("enum", key, fData.get(key));
			} catch (Exception e) {
				throw new ConfigurationInvalidTypeException("enum", key, "null");
			}
		}
	}

	public <T extends Enum<T>> T getEnum(String key, Class<T> enumClass, T defaultValue) {
		String value = getString(key, defaultValue.toString()).toUpperCase();

		try {
			return Enum.valueOf(enumClass, value);
		} catch (Exception ex) {
			try {
				throw new ConfigurationInvalidTypeException("enum", key, fData.get(key));
			} catch (NullPointerException e) {
				return defaultValue;
			}
		}
	}

	public Config getSubConfig(String key, String type) {
		ensureExists(key);
		try {
			return new Config(type, (Map) fData.get(key));
		} catch (ClassCastException ex) {
			throw new ConfigurationException("***** Value for " + key + " was not a fConfig *****");
		}
	}

	private void ensureExists(String key) {
		if (!fData.containsKey(key)) {
			throw new ConfigurationException("***** Must provide a value for " + key + " for configuring " + fType + " ***** ");
		}
	}

	public String toString() {
		return fData.toString();
	}
}