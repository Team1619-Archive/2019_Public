package org.team1619.utilities;

public class Timer {

	private long startTime = -1;
	private long time;

	public void start(long timeMs) {
		this.time = timeMs;
		this.startTime = System.currentTimeMillis();
	}

	public void reset() {
		this.startTime = -1;
	}

	public boolean isStarted() {
		return this.startTime > -1;
	}

	public boolean isDone() {
		return startTime != -1 && System.currentTimeMillis() - startTime >= time;
	}

}
