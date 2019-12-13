package org.team1619.services.logging;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class LoggingService extends AbstractScheduledService {
	private static final Logger sLogger = LogManager.getLogger(LoggingService.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final RobotConfiguration fRobotConfiguration;
	private final Dashboard fDashboard;
	private double fPreviousTime;
	private long MIN_FRAME_TIME;


	private Set<String> fDesiredLogs = new HashSet<>();

	@Inject

	public LoggingService(InputValues inputValues, OutputValues outputValues, RobotConfiguration robotConfiguration, Dashboard dashboard) {
		fSharedInputValues = inputValues;
		fRobotConfiguration = robotConfiguration;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
	}

	@Override
	protected void startUp() throws Exception {
		sLogger.info("Starting LoggingService");

		Map<String, Object> logConfig = fRobotConfiguration.getCategory("log");
		String valuesToLog = "Logging the following values: [";
		for (String name : logConfig.keySet()) {
			if ((Boolean) logConfig.get(name)) {
				fDesiredLogs.add(name);
				valuesToLog += (name + ", ");
			}
		}

		valuesToLog = valuesToLog.substring(0, valuesToLog.length() - 2) + "]";
		sLogger.debug(valuesToLog);

		fPreviousTime = -1;
		MIN_FRAME_TIME = fRobotConfiguration.getInt("global_timing", "logger_service_max_frame_time");

		fDashboard.initialize();

		sLogger.info("LoggingService started");
	}

	@Override
	protected void runOneIteration() throws Exception {
		for (String name : fDesiredLogs) {
			String type = name.substring(0, 2);
			switch (type) {
				case "ni":
					fDashboard.putNumber(name, fSharedInputValues.getNumeric(name));
					break;
				case "bi":
					fDashboard.putBoolean(name, fSharedInputValues.getBoolean(name));
					break;
				case "vi":
					Map<String, Double> vectorInput = fSharedInputValues.getVector(name);
					for (String key : vectorInput.keySet()) {
						fDashboard.putNumber(key, vectorInput.get(key));
					}
					break;
				case "mo":
					fDashboard.putNumber(name, fSharedOutputValues.getMotorOutputValue(name));
					break;
				case "so":
					fDashboard.putBoolean(name, fSharedOutputValues.getSolenoidOutputValue(name));
					break;
				case "si":
					fDashboard.putString(name, fSharedInputValues.getString(name));
					break;
				default:
					throw new RuntimeException("The value type could not be determed for '" + name + "'. Ensure that it follows naming convention and matches " +
							"its name from its yaml file. ");
			}
		}

		// Check for delayed frames
		fPreviousTime = (fPreviousTime < 0) ? System.currentTimeMillis() : fPreviousTime;
		double currentTime = System.currentTimeMillis();
		double diffTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_logging_service_frame_time", diffTime);
		if (diffTime > MIN_FRAME_TIME) {
			sLogger.info("********** Logging Service frame time = {}", diffTime);
		}
		fPreviousTime = currentTime;

		//Check for auto selection
		if(fDashboard.autoSelectionRisingEdge()) {
			fDashboard.smartdashboardSetAuto();
		}

	}

	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1000 / 60, TimeUnit.MILLISECONDS);
	}

}
