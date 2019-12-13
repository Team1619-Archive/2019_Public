package org.team1619.shared.abstractions;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RobotConfiguration {

	void initialize();

	List<String> getStateNames();

	Set<String> getSubsystemNames();

	Set<String> getBooleanInputNames();

	Set<String> getNumericInputNames();

	Set<String> getVectorInputNames();

	Set<String> getMotorNames();

	Set<String> getSolenoidNames();

	Object get(String category, String key);

	Map<String, Object> getCategory(String category);

	int getInt(String category, String key);

	double getDouble(String category, String key);

	boolean getBoolean(String category, String key);

	String getString(String category, String key);

	List getList(String category, String key);

	Set getSet(String category, String key);

	<T extends Enum<T>> T getEnum(String category, String key, Class<T> enumClass);
}

