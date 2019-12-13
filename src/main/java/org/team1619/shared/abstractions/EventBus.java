package org.team1619.shared.abstractions;

public interface EventBus {
	void register(Object object);

	void post(Object object);

	void unregister(Object object);
}
