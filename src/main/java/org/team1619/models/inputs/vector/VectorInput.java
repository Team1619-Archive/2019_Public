package org.team1619.models.inputs.vector;

import org.team1619.utilities.Config;

import javax.annotation.Nullable;
import java.util.Map;

public abstract class VectorInput {

	protected final Object fName;

	public VectorInput(Object name, Config config) {
		fName = name;
	}

	public abstract void initialize();

	public abstract void update();

	public abstract Map<String, Double> get(@Nullable Object flag);

	public Object getName() {
		return fName;
	}
}
