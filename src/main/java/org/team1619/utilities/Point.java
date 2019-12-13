package org.team1619.utilities;

/**
 * Point is a simple class which stores an x and y value for a point,
 * and can do simple operations on the point
 *
 * @author Matthew Oates
 */

public class Point {

	protected final double fX, fY;

	public Point(double x, double y) {
		fX = x;
		fY = y;
	}

	public double getX() {
		return fX;
	}

	public double getY() {
		return fY;
	}

	public Point add(Point point) {
		return new Point(fX + point.getX(), fY + point.getY());
	}

	public Point subtract(Point point) {
		return new Point(fX - point.getX(), fY - point.getY());
	}

	public double distance(Point point) {
		return Math.sqrt(Math.pow(point.fX - fX, 2) + Math.pow(point.fY - fY, 2));
	}

	public String toString() {
		return "(" + String.format("%.4f", fX) + "," + String.format("%.4f", fY) + ")";
	}
}
