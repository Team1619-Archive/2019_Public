package org.team1619.utilities;

import javax.annotation.Nullable;

public class TrapezoidTrajectory {

	private double fDistance, fMaxVelocity, fAcceleration, fDeceleration, fStartVelocity, fEndVelocity;
	@Nullable
	private TrajectoryPoint[] fTrajectoryPoints;
	private double fTime = 0.0;
	private int fIndex;
	@Nullable
	private TrajectoryPoint fTrajectoryPoint, fNextTrajectoryPoint;

	public TrapezoidTrajectory(double distance, double maxVelocity, double acceleration) {
		this(distance, maxVelocity, acceleration, acceleration);
	}

	public TrapezoidTrajectory(double distance, double maxVelocity, double acceleration, double startVelocity) {
		this(distance, maxVelocity, acceleration, acceleration, startVelocity, 0.0);
	}

	public TrapezoidTrajectory(double distance, double maxVelocity, double acceleration, double deceleration, double startVelocity, double endVelocity) {
		fDistance = distance;
		fMaxVelocity = maxVelocity;
		fAcceleration = acceleration;
		fDeceleration = deceleration;
		fStartVelocity = startVelocity;
		fEndVelocity = endVelocity;
	}

	public void calculate(int resolution) {
		assert resolution >= 1;
		fTrajectoryPoints = new TrajectoryPoint[resolution + 1];

		double deltaDistance = fDistance / resolution;
		double velocity = fStartVelocity;
		fTrajectoryPoints[0] = new TrajectoryPoint(0.0, velocity);
		for (int i = 1; i < resolution + 1; i++) {
			double distance = deltaDistance * i;
			double maxAchievableVelocity = Math.sqrt(Math.pow(velocity, 2) + 2 * fAcceleration * deltaDistance);    // vf^2 = vi^2 + 2ad
			velocity = Math.min(maxAchievableVelocity, fMaxVelocity);

			fTrajectoryPoints[i] = new TrajectoryPoint(distance, velocity);
		}

		velocity = fEndVelocity;
		fTrajectoryPoints[resolution].fVelocity = velocity;
		for (int i = resolution - 1; i > 0; i--) {
			double maxAchievableVelocity = Math.sqrt(Math.pow(velocity, 2) + 2 * fDeceleration * deltaDistance);
			velocity = Math.min(maxAchievableVelocity, fTrajectoryPoints[i].fVelocity);

			fTrajectoryPoints[i].fVelocity = velocity;
		}

		TrajectoryPoint previous = fTrajectoryPoints[0];
		for (int i = 1; i < resolution + 1; i++) {
			TrajectoryPoint current = fTrajectoryPoints[i];
			previous.fAcceleration = (Math.pow(current.fVelocity, 2) - Math.pow(previous.fVelocity, 2)) / (2.0 * deltaDistance);

			double time;
			double deltaVelocity = (current.fVelocity - previous.fVelocity);
			if (deltaVelocity > 0) {
				time = deltaVelocity / fAcceleration;
			} else if (deltaVelocity < 0) {
				time = deltaVelocity / -fDeceleration;
			} else {
				//todo - when the elevator does need to move, velocity is zero because maxAchievableVelocity is zero because delta distance is zero
				//todo - this causes a divide by zero and time = NaN
				time = (current.fDistance - previous.fDistance) / current.fVelocity;
			}
			fTime += time;

			previous = current;
		}
		previous.fAcceleration = 0.0;

		this.reset();

//		System.out.println(print(resolution));
	}

	public void reset() {
		if(fTrajectoryPoints == null) {
			throw new NullPointerException("fTrajectoryPoints is null");
		}
		assert fTrajectoryPoints.length >= 2;

		fIndex = 0;
		fTrajectoryPoint = fTrajectoryPoints[fIndex];
		fNextTrajectoryPoint = fTrajectoryPoints[fIndex + 1];
	}

	public TrajectoryPoint getPoint(double distance) {
		if(fTrajectoryPoints == null) {
			throw new NullPointerException("fTrajectoryPoints is null");
		}

		if (fIndex < fTrajectoryPoints.length && distance >= fDistance) {
			fIndex = fTrajectoryPoints.length;
		}
		if (fIndex == fTrajectoryPoints.length) {
			return new TrajectoryPoint(distance, fEndVelocity, 0.0);
		}

		if(fNextTrajectoryPoint == null) {
			throw new NullPointerException("fNextTrajectoryPoint is null");
		}
		if (distance > fNextTrajectoryPoint.fDistance) {
			do {
				fIndex++;
				fNextTrajectoryPoint = fTrajectoryPoints[fIndex + 1];
			} while (distance > fNextTrajectoryPoint.fDistance);
			fTrajectoryPoint = fTrajectoryPoints[fIndex];
		}

		if(fTrajectoryPoint == null) {
			throw new NullPointerException("fTrajectoryPoint is null");
		}
		double velocity = (fNextTrajectoryPoint.fVelocity - fTrajectoryPoint.fVelocity) / (fNextTrajectoryPoint.fDistance - fTrajectoryPoint.fDistance) * (distance - fTrajectoryPoint.fDistance) + fTrajectoryPoint.fVelocity;
		double acceleration = (fNextTrajectoryPoint.fAcceleration - fTrajectoryPoint.fAcceleration) / (fNextTrajectoryPoint.fDistance - fTrajectoryPoint.fDistance) * (distance - fTrajectoryPoint.fDistance) + fTrajectoryPoint.fAcceleration;
		return new TrajectoryPoint(distance, velocity, acceleration);
	}

	public double getDistance() {
		return fDistance;
	}

	public String print(double resolution) {
		String desmos = "";
		for (int i = 0; i < resolution; i++) {
			double distance = getDistance() / resolution * i;
			TrajectoryPoint current = getPoint(distance);
			desmos += "(" + distance + "," + current.fVelocity + "),";
		}

		return desmos.substring(0, desmos.length() - 1);
	}

	public double getTime() {
		return fTime;
	}

	public static void main(String[] args) {
		TrapezoidTrajectory trajectory = new TrapezoidTrajectory(51.0, 79.1, 217.4);
		trajectory.calculate(100);
		System.out.print(trajectory.getTime());
	}
}