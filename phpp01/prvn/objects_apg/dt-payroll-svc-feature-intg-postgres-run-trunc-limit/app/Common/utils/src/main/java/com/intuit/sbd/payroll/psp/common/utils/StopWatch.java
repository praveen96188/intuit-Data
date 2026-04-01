package com.intuit.sbd.payroll.psp.common.utils;

/**
 * User: rnorian
 * Date: Oct 13, 2008
 *
 * TODO: convert to using thread local by default and static methods for start()/stop()/elapsed()
 */
public class StopWatch {
    private static final long MILLISPERSECOND = 1000;
    private static final long MILLISPERMINUTE = MILLISPERSECOND * 60;
    private static final long MILLISPERHOUR = MILLISPERMINUTE * 60;

    private String name;
    private long start;
    private long stop;
    private boolean isRunning;

    public StopWatch() {
        this(null);
    }

    public StopWatch(String pName) {
        name = pName;
    }

    public static StopWatch startTimer() {
        return create(true);
    }

    public static StopWatch startTimer(String pName) {
        return create(pName, true);
    }

    public static StopWatch create(boolean start) {
        return create(null, start);
    }

    public static StopWatch create(String pName, boolean pStart) {
        StopWatch stopWatch = new StopWatch(pName);
        if (pStart)
            stopWatch.start();
        return stopWatch;
    }

    public void reset() {
        start = -1;
        stop = -1;
        isRunning = false;
    }

    public StopWatch start() {
        reset();
        isRunning = true;
        start = System.currentTimeMillis();
        return this;
    }

    public StopWatch stop() {
        stop = System.currentTimeMillis();
        isRunning = false;
        return this;
    }

    public long getElapsedMillis() {
        long elapsed = -1;

        if (isRunning)
            elapsed = System.currentTimeMillis() - start;
        else
            elapsed = stop - start;

        return elapsed;
    }

    public double getElapsedSeconds() {
        return getElapsedMillis() / 1000d;
    }

    public double getElapsedMinutes() {
        return getElapsedMillis() / 1000d / 60d;
    }

    public String getElapsedTimeString() {
        long millis = (isRunning ? System.currentTimeMillis() : stop) - start;

        long hours = millis / MILLISPERHOUR;
        millis %= MILLISPERHOUR;

        long minutes = millis / MILLISPERMINUTE;
        millis %= MILLISPERMINUTE;

        long seconds = millis / MILLISPERSECOND;
        millis %= MILLISPERSECOND;

        StringBuffer elapsed = new StringBuffer();

        if (hours > 0) {
            elapsed.append(hours);
            elapsed.append("h ");
        }

        if ((elapsed.length() > 0) || (minutes > 0)) {
            elapsed.append(minutes);
            elapsed.append("m ");
        }

        if ((elapsed.length() > 0) || (seconds > 0)) {
            elapsed.append(seconds);
            elapsed.append("s ");
        }

        elapsed.append(millis);
        elapsed.append("ms");

        return elapsed.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StopWatch: ");
        if (name != null) {
            sb.append(name);
        }
        sb.append("\t").append(getElapsedTimeString());
        return sb.toString();
    }
}
