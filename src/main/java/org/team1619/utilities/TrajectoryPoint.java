package org.team1619.utilities;

/**
 * TrajectoryPoint stores information about specific points along a 1d trajectory
 */

public class TrajectoryPoint {

	public double fDistance, fVelocity, fAcceleration;

	public TrajectoryPoint(double distance, double velocity) {
		this(distance, velocity, 0.0);
	}

	public TrajectoryPoint(double distance, double velocity, double acceleration) {
		fDistance = distance;
		fVelocity = velocity;
		fAcceleration = acceleration;
	}

	public static TrajectoryPoint invert(TrajectoryPoint trajectoryPoint) {
		return new TrajectoryPoint(trajectoryPoint.fDistance, -trajectoryPoint.fVelocity, -trajectoryPoint.fAcceleration);
	}
}