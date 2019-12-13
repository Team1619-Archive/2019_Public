package org.team1619.shared.concretions;

import org.team1619.utilities.injection.Singleton;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.FMS;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class SharedFMS implements FMS {

	private static final Logger sLogger = LogManager.getLogger(SharedFMS.class);

	private Map<String, Mode> fData = new ConcurrentHashMap<>();

	@Override
	public void setMode(Mode mode) {
		fData.put("mode", mode);

		sLogger.info("FMS mode set to '{}'", mode);
	}

	@Override
	public Mode getMode() {
		return fData.getOrDefault("mode", Mode.DISABLED);
	}
}
