package org.team1619.models.inputs.vector;

import org.team1619.utilities.logging.LogManager;
import org.team1619.utilities.logging.Logger;
import org.team1619.shared.abstractions.InputValues;
import org.team1619.utilities.Config;
import org.team1619.utilities.Pose2d;
import org.team1619.utilities.Vector;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * OdometryInput is a VectorInput which uses the navx and drive encoders,
 * to track the robots current position relative to its starting point
 *
 * @author Matthew Oates
 */

public class OdometryInput extends VectorInput {

	private static final Logger sLogger = LogManager.getLogger(OdometryInput.class);

	protected final Config fConfig;
	protected final InputValues fSharedInputValues;

	private final String fNavx;
	private Map<String, Double> fNavxValues = new HashMap<>();

	private final String fLeftEncoder;
	private final String fRightEncoder;

	private double fLeftPosition = 0;
	private double fRightPosition = 0;
	private double fHeading = 0;

	private Pose2d fCurrentPosition;

	public OdometryInput(Object name, Config config, InputValues inputValues) {
		super(name, config);

		fConfig = config;
		fSharedInputValues = inputValues;

		fNavx = config.getString("navx");

		fLeftEncoder = config.getString("left_encoder");
		fRightEncoder = config.getString("right_encoder");

		fCurrentPosition = new Pose2d();
	}

	@Override
	public void initialize() {

	}

	@Override
	public void update() {
		if (!fSharedInputValues.getBoolean("bi_odometry_has_been_zeroed")) {
			zeroPosition();
			fSharedInputValues.setBoolean("bi_odometry_has_been_zeroed", true);
			sLogger.info("Odometry Input -> Zeroed");
			return;
		}

		fHeading = getHeading();

		double leftPosition = fSharedInputValues.getNumeric(fLeftEncoder, null);
		double rightPosition = fSharedInputValues.getNumeric(fRightEncoder, null);

		/*
		"distance" is the straight line distance the robot has traveled since the last iteration

		We calculate the straight line distance the robot has traveled by averaging the left and right encoder deltas.
		This works because we take readings so frequently that curvature doesn't impact the straight line distance
		enough to accumulate much error.

		This could be improved later but in our testing it worked well.
		 */
		double distance = ((leftPosition - fLeftPosition) + (rightPosition - fRightPosition)) / 2;

		fCurrentPosition = new Pose2d(fCurrentPosition.add(new Vector(distance * Math.cos(Math.toRadians(fHeading)), distance * Math.sin(Math.toRadians(fHeading)))), fHeading);

		fLeftPosition = leftPosition;
		fRightPosition = rightPosition;
	}

	public void zeroPosition() {
		fLeftPosition = fSharedInputValues.getNumeric(fLeftEncoder, null);
		fRightPosition = fSharedInputValues.getNumeric(fRightEncoder, null);
		fCurrentPosition = new Pose2d();
	}

	@Override
	public Map<String, Double> get(@Nullable Object flag) {
		return Map.of("x", fCurrentPosition.getX(), "y", fCurrentPosition.getY(), "heading", fCurrentPosition.getHeading());
	}

	private double getHeading() {
		fNavxValues = fSharedInputValues.getVector(fNavx, null);
		double heading = fNavxValues.getOrDefault("fused_heading", 0.0);

		if (heading > 180) heading = -(360 - heading);

		//Inverts the heading to so that positive angle is counterclockwise, this makes trig functions work properly
		heading = -heading;

		return heading;
	}
}
