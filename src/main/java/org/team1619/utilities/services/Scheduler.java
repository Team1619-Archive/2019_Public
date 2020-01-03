package org.team1619.utilities.services;

/**
  *  Keeps track of the timing for services
 */


public class Scheduler {

    private final double fInitialDelay;
    private final double fStandardDelay;
    private final TimeUnit fTimeUnit;

    private long fStartTime = 0;
    private long fLastTime = 0;

    // Standard delay: min time each frame can run
    // Initial delay: delay before first frame
    // Time unit: milliseconds, seconds, minutes
    public Scheduler(double standardDelay) {
        this(0, standardDelay);
    }

    public Scheduler(double initialDelay, double standardDelay) {
        this(initialDelay, standardDelay, TimeUnit.MILLISECOND);
    }

    public Scheduler(double standardDelay, TimeUnit timeUnit) {
        this(0, standardDelay, timeUnit);
    }

    public Scheduler(double initialDelay, double standardDelay, TimeUnit timeUnit) {
        fInitialDelay = initialDelay;
        fStandardDelay = standardDelay;
        fTimeUnit = timeUnit;
    }

    // Called on start-up
    public synchronized void start() {
        fStartTime = System.currentTimeMillis();
    }

    // Called every frame
    public synchronized void run() {
        fStartTime = 0;
        fLastTime = System.currentTimeMillis();
    }

    // Determines whether a frame has reached it's min time
    public synchronized boolean shouldRun() {
        long currentTime = System.currentTimeMillis();

        if (fStartTime != 0 && currentTime - fStartTime < fTimeUnit.toMilliseconds(fInitialDelay)) {
            return false;
        }

        return fLastTime == 0 || !(currentTime - fLastTime < fTimeUnit.toMilliseconds(fStandardDelay));
    }

    // Returns the amount of time until the next time the service should run
    public synchronized long millisecondsUntilNextRun() {
        long currentTime = System.currentTimeMillis();

        if (fStartTime != 0) {
            long time = (int)(fTimeUnit.toMilliseconds(fInitialDelay) - (currentTime - fLastTime));
            if(time < 0) {
                return 0;
            }
            return time;
        }

        long time = (int)(fTimeUnit.toMilliseconds(fStandardDelay) - (currentTime - fLastTime));
        if(time < 0) {
            return 0;
        }
        return time;
    }

    public enum TimeUnit {
        MINUTE(60000),
        SECOND(1000),
        MILLISECOND(1);

        private double toMillisecond;

        TimeUnit(double toMillisecond) {
            this.toMillisecond = toMillisecond;
        }

        public synchronized double toMilliseconds(double time) {
            return time * toMillisecond;
        }
    }
}
