package org.team1619.utilities;

/**
 * VelocityPIDFProfile keeps track of PIDF and counts per 100 ms values for motion magic velocity control mode
 *
 * @author Matthew Oates
 */

public class VelocityPIDFProfile extends PIDFProfile {

	private final double fCountsPer100ms;

	public VelocityPIDFProfile(double p, double i, double d, double f, double countsPer100ms) {
		super(p, i, d, f);
		fCountsPer100ms = countsPer100ms;
	}

	public VelocityPIDFProfile(double p, double i, double d, double countsPer100ms) {
		this(p, i, d, 0, countsPer100ms);
	}

	public VelocityPIDFProfile() {
		this(0, 0, 0, 0);
	}

	public double getCountsPer100ms() {
		return fCountsPer100ms;
	}
}
