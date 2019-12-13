package org.team1619.utilities;

/**
 * PathPoint is an add on to the Point class,
 * allowing it to store additional values necessary for the pure pursuit Path class
 *
 * @author Matthew Oates
 */

public class PathPoint extends Point {

	private final double distance, curvature;
	private double velocity;

	public PathPoint(double x, double y, double distance, double curvature, double velocity) {
		super(x, y);
		this.distance = distance;
		this.curvature = curvature;
		this.velocity = velocity;
	}

	public PathPoint(Point point, double distance, double curvature, double velocity) {
		this(point.getX(), point.getY(), distance, curvature, velocity);
	}

	public double getDistance() {
		return distance;
	}

	public double getCurvature() {
		return curvature;
	}

	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
	}
}
