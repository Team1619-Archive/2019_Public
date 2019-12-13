package org.team1619.utilities;

/**
 * Vector is an add on to the Point class,
 * allowing it to preform vector operations
 *
 * @author Matthew Oates
 */

public class Vector extends Point {

	public Vector(double x, double y) {
		super(x, y);
	}

	public Vector(Point point) {
		this(point.getX(), point.getY());
	}

	public Vector(double x1, double y1, double x2, double y2) {
		this(x2 - x1, y2 - y1);
	}

	public Vector(Point point1, Point point2) {
		this(point1.getX(), point1.getY(), point2.getX(), point2.getY());
	}

	public double magnitude() {
		return Math.sqrt(Math.pow(fX, 2) + Math.pow(fY, 2));
	}

	public Vector normalize() {
		return new Vector((1 / magnitude()) * fX, (1 / magnitude()) * fY);
	}

	public Vector scale(double scalar) {
		return new Vector(fX * scalar, fY * scalar);
	}

	public double dot(Vector vector) {
		return fX * vector.getX() + fY * vector.getY();
	}

	public String toString() {
		return "<" + fX + "," + fY + ">";
	}
}
