package org.team1619.services.logging;

import org.team1619.utilities.injection.Inject;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.Dashboard;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.shared.abstractions.OutputValues;
import org.team1619.shared.abstractions.RobotConfiguration;
import org.team1619.utilities.services.ScheduledService;
import org.team1619.utilities.services.Scheduler;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoggingService extends ScheduledService {
	private static final Logger sLogger = LogManager.getLogger(LoggingService.class);

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final RobotConfiguration fRobotConfiguration;
	private final Dashboard fDashboard;
	private double fPreviousTime;
	private long FRAME_TIME_THRESHOLD;
	private long FRAME_CYCLE_TIME_THRESHOLD;


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
		FRAME_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_time_threshold_logging_service");
		FRAME_CYCLE_TIME_THRESHOLD = fRobotConfiguration.getInt("global_timing", "frame_cycle_time_threshold_logging_service");

		fDashboard.initialize();

		sLogger.info("LoggingService started");
	}

	@Override
	protected void runOneIteration() throws Exception {

		double frameStartTime = System.currentTimeMillis();

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
					fDashboard.putNumber(name, (double) fSharedOutputValues.getMotorOutputs(name).get("value"));
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




		//Check for auto selection
		if(fDashboard.autoSelectionRisingEdge()) {
			fDashboard.smartdashboardSetAuto();
		}


		// Check for delayed frames
		double currentTime = System.currentTimeMillis();
		double frameTime = currentTime - frameStartTime;
		double totalCycleTime = currentTime - fPreviousTime;
		fSharedInputValues.setNumeric("ni_frame_time_logging_service", frameTime);
		fSharedInputValues.setNumeric("ni_frame_cycle_time_logging_service", totalCycleTime);
		if (frameTime > FRAME_TIME_THRESHOLD) {
			sLogger.info("********** Logging Service frame time = {}", frameTime);
		}
		if (totalCycleTime > FRAME_CYCLE_TIME_THRESHOLD) {
			sLogger.info("********** Logging Service frame cycle time = {}", totalCycleTime);
		}
		fPreviousTime = currentTime;
	}

	@Override
	protected void shutDown() throws Exception {

	}

	@Override
	protected Scheduler scheduler() {
		return new Scheduler(1000 / 60);
	}
}
