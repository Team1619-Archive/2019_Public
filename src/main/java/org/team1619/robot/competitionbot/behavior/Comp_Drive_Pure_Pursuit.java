package org.team1619.robot.competitionbot.behavior;

import org.team1619.utilities.Timer;
import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.models.behavior.Behavior;
import org.team1619.models.outputs.motors.Motor;
import org.team1619.shared.abstractions.*;
import org.team1619.utilities.*;
import java.util.*;

/**
 * Follows a path using pure pursuit
 */

public class Comp_Drive_Pure_Pursuit implements Behavior {

	private static final Logger sLogger = LogManager.getLogger(Comp_Drive_Pure_Pursuit.class);
	private static final Set<String> sSubsystems = Set.of("ss_drive");

	private final InputValues fSharedInputValues;
	private final OutputValues fSharedOutputValues;
	private final Dashboard fDashboard;
	private String fStateName;
	private String fPathName;

	private final Map<String, Path> fPaths = new HashMap<>();

	private double fTrackWidth;

	private final int GEAR_SHIFT_DELAY_TIME;
	private Path fCurrentPath = new Path();
	private Pose2d fCurrentPosition = new Pose2d();
	private boolean fIsFollowing = true;
	private boolean fIsReversed = false;
	private Timer fGearshiftTimer;

	public Comp_Drive_Pure_Pursuit(InputValues inputValues, OutputValues outputValues, Config config, Dashboard dashboard, RobotConfiguration robotConfiguration) {
		fSharedInputValues = inputValues;
		fSharedOutputValues = outputValues;
		fDashboard = dashboard;
		fStateName = "Unknown";
		fPathName = "Unknown";

		fTrackWidth = robotConfiguration.getDouble("global_drive", "pure_pursuit_track_width");
		GEAR_SHIFT_DELAY_TIME = robotConfiguration.getInt("global_drive", "gear_shift_delay");
		fGearshiftTimer = new Timer();

		// Creates and stores path objects from the paths config file, in the fPaths map
		YamlConfigParser yamlConfigParser = new YamlConfigParser();
		yamlConfigParser.loadWithFolderName("paths.yaml");

		Map<String, Map<String, Object>> paths = ((Map<String, Map<String, Object>>) yamlConfigParser.getData().get("paths"));
		if (paths != null) {
			for (String pathName : paths.keySet()) {
				Config pathConfig = yamlConfigParser.getConfig(pathName);

				ArrayList<Point> points = new ArrayList<>();

				pathConfig.getList("path").forEach((p) -> {
					List point = (List) p;
					points.add(new Point(point.get(0) instanceof Integer ? (int) point.get(0) : (double) point.get(0), point.get(1) instanceof Integer ? (int) point.get(1) : (double) point.get(1)));
				});

				Path path = new Path(points);

				path.setPointSpacing(pathConfig.getDouble("spacing", path.getPointSpacing()));
				path.setPathSmoothing(pathConfig.getDouble("smoothing", path.getPathSmoothing()));
				path.setTurnSpeed(pathConfig.getDouble("turn_speed", path.getTurnSpeed()));
				path.setTrackingErrorSpeed(pathConfig.getDouble("tracking_error_speed", path.getTrackingErrorSpeed()));
				path.setMaxAcceleration(pathConfig.getDouble("max_acceleration", path.getMaxAcceleration()));
				path.setMinSpeed(pathConfig.getDouble("min_speed", path.getMinSpeed()));
				path.setMaxSpeed(pathConfig.getDouble("max_speed", path.getMaxSpeed()));
				path.setLookAheadDistance(pathConfig.getDouble("look_ahead_distance", path.getLookAheadDistance()));
				path.setVelocityLookAheadPoints(pathConfig.getInt("velocity_look_ahead_points", path.getVelocityLookAheadPoints()));

				path.build();

				fPaths.put(pathName, path);
			}
		}
	}

	@Override
	public void initialize(String stateName, Config config) {
		sLogger.info("Entering state {}", stateName);
		fStateName = stateName;

		fPathName = config.getString("path_name");

		if (!fPaths.containsKey(fPathName)) {
			sLogger.error("Path " + fPathName + " doesn't exist");
			fCurrentPath = new Path();
		} else {
			fCurrentPath = fPaths.get(fPathName);
		}

		WebDashboardGraphDataset pathGraphDataset = new WebDashboardGraphDataset();

		for (Point point : fCurrentPath.getPoints()) {
			pathGraphDataset.addPoint(point.getX(), point.getY());
		}

		fSharedInputValues.setVector("gr_" + stateName, pathGraphDataset);

		fIsReversed = config.getBoolean("reversed", false);

		fCurrentPath.reset();

		fIsFollowing = true;

		fGearshiftTimer.reset();
		if(fSharedInputValues.getBoolean("bi_leaving_ll_direct")) {
			fSharedInputValues.setBoolean("bi_leaving_ll_direct", false);
			fGearshiftTimer.start(GEAR_SHIFT_DELAY_TIME);
		}
	}

	@Override
	public void update() {
		if (!fIsFollowing || !fSharedInputValues.getBoolean("bi_odometry_has_been_zeroed")) {
			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);

			return;
		}

		Map<String, Double> odometryValues = fSharedInputValues.getVector("vi_odometry");

		// Turns the odometry values into a Pose2d to pass to path methods
		fCurrentPosition = new Pose2d(odometryValues.get("x"), odometryValues.get("y"), odometryValues.get("heading"));

		Pose2d followPosition = fCurrentPosition.clone();

		if (fIsReversed) {
			followPosition = new Pose2d(followPosition.getX(), followPosition.getY(), ((followPosition.getHeading() + 360) % 360) - 180);
		}

		int lookahead = fCurrentPath.getLookAheadPointIndex(followPosition);
		int closest = fCurrentPath.getClosestPointIndex(followPosition);

		if (lookahead == -1) {
			fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
			fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);

			fIsFollowing = false;
			return;
		}

		// Uses the path object to calculate curvature and velocity values
		double velocity = fCurrentPath.getPathPointVelocity(closest, followPosition);
		double curvature = fCurrentPath.getCurvatureFromPathPoint(lookahead, followPosition);

		double left;
		double right;

		// Calculates wheel velocities based upon the curvature and velocity values calculated by the path
		if (fIsReversed) {
			left = -(velocity * ((1.5 - curvature * fTrackWidth) / 1.5));
			right = -(velocity * ((1.5 + curvature * fTrackWidth) / 1.5));
		} else {
			left = (velocity * ((1.5 + curvature * fTrackWidth) / 1.5));
			right = (velocity * ((1.5 - curvature * fTrackWidth) / 1.5));
		}

		//sLogger.info("Velocity: {}, Curvature: {}, Left: {}, Right: {}, Lookahead: ({}, {})", velocity, curvature, left, right, fCurrentPath.getPoint(lookahead).getX(), fCurrentPath.getPoint(lookahead).getY());
		fSharedInputValues.setVector("vi_pure_pursuit", Map.of("velocity", velocity, "curvature", curvature, "left", left, "right", right, "cx", fCurrentPath.getPoint(closest).getX(), "cy", fCurrentPath.getPoint(closest).getY(), "lx", fCurrentPath.getPoint(lookahead).getX(), "ly", fCurrentPath.getPoint(lookahead).getY()));

		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.VELOCITY_CONTROLLED, left, "pr_pure_pursuit");
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.VELOCITY_CONTROLLED, right, "pr_pure_pursuit");

		if(fGearshiftTimer.isDone() || !fGearshiftTimer.isStarted()) {
			// shift gears
			fSharedOutputValues.setSolenoidOutputValue("so_gear_shifter", false);
			fSharedInputValues.setBoolean("bi_is_low_gear", false);
		}
	}

	@Override
	public void dispose() {
		sLogger.debug("Leaving state {}", fStateName);
		fSharedOutputValues.setMotorOutputValue("mo_drive_left", Motor.OutputType.PERCENT, 0.0, null);
		fSharedOutputValues.setMotorOutputValue("mo_drive_right", Motor.OutputType.PERCENT, 0.0, null);
	}

	@Override
	public boolean isDone() {
		return !fIsFollowing;
	}

	@Override
	public boolean isRequestingDisposal() {
		return false;
	}

	@Override
	public Set<String> getSubsystems() {
		return sSubsystems;
	}
}