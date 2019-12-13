package org.team1619.utilities;

/**
 * PIDFProfile keeps track of PIDF values for motion magic
 *
 * @author Matthew Oates
 */

public class PIDFProfile {

	private final double fP;
	private final double fI;
	private final double fD;
	private final double fF;

	public PIDFProfile(double p, double i, double d, double f) {
		fP = p;
		fI = i;
		fD = d;
		fF = f;
	}

	public PIDFProfile(double p, double i, double d) {
		this(p, i, d, 0);
	}

	public PIDFProfile() {
		this(0, 0, 0);
	}

	public double getP() {
		return fP;
	}

	public double getI() {
		return fI;
	}

	public double getD() {
		return fD;
	}

	public double getF() {
		return fF;
	}

	public boolean equals(PIDFProfile profile) {
		return fP == profile.getP() && fI == profile.fI && fD == profile.getD() && fF == profile.getF();
	}
}
