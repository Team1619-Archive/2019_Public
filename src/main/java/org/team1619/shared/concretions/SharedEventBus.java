package org.team1619.shared.concretions;

import org.team1619.utilities.eventbus.AsyncEventBus;
import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.EventBus;

import java.util.concurrent.Executors;

@Singleton
public class SharedEventBus implements EventBus {

	private static final Logger sLogger = LogManager.getLogger(SharedEventBus.class);

	private org.team1619.utilities.eventbus.EventBus fEventBus;

	public SharedEventBus() {
		fEventBus = new AsyncEventBus(Executors.newFixedThreadPool(4));
	}

	@Override
	public void register(Object object) {
		sLogger.debug("Registering object '{}'", object);

		fEventBus.register(object);
	}

	@Override
	public void post(Object object) {
		sLogger.debug("Posting object '{}'", object);

		fEventBus.post(object);
	}

	@Override
	public void unregister(Object object) {
		sLogger.info("Unregistering object '{}'", object);

		fEventBus.unregister(object);
	}


}
