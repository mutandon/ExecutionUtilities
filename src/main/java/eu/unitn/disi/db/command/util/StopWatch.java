/*
 * Copyright (C) 2012 Davide Mottin <mottin@disi.unitn.eu>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package eu.unitn.disi.db.command.util;

import static eu.unitn.disi.db.command.util.StopWatch.TimeType.APPLICATION;
import static eu.unitn.disi.db.command.util.StopWatch.TimeType.CPU;
import static eu.unitn.disi.db.command.util.Time.getCpuTime;
import static eu.unitn.disi.db.command.util.Time.getCpuTime;
import static eu.unitn.disi.db.command.util.Time.getSystemTime;
import static eu.unitn.disi.db.command.util.Time.getUserTime;
import static java.lang.System.nanoTime;

/**
 * This class implements a stop watch to measure the actual CPU time*.
 *
 * <p>
 *  *Please notice that the actual CPU time is not the time spent in the execution
 *  but the time in which the thread actually uses the CPU
 * </p>
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class StopWatch {

    private long startTime = 0;
    private long stopTime = 0;
    private long[] ids = null;
    private boolean running = false;
    private TimeType type;

    /**
     * Represents the time type
     */
    public enum TimeType {
        /**
         * CPU time is the effective time spent by the process  (USER + SYSTEM time)
         */
        CPU,
        /**
         * User time is the time spent only in user code tasks
         */
        USER,
        /**
         * Time spent in I/Os by the system (without user code)
         */
        SYSTEM,
        /**
         * Application time is the time spent in the process itself (System.nanoseconds)
         */
        APPLICATION;
    }

    /**
     * Creates a stopwatch with TimeType.APPLICATION
     */
    public StopWatch() {
        this(APPLICATION);
    }

    /**
     * Creates a stopwatch measuring the specific type
     * @param tt
     */
    public StopWatch(TimeType tt) {
        type = tt;
    }

    /**
     * Creates a stopwatch measuring the CPU type with  a list of thread IDs
     * @param tt
     */
    public StopWatch(long ids[]) {
        this(CPU);
        this.ids = ids;
    }


    /*
     * Get the time based on the input type
     */
    private long getTime() {
        switch(type) {
            case CPU:
                return ids == null ? getCpuTime() : getCpuTime(this.ids);
            case USER:
                return getUserTime();
            case SYSTEM:
                return getSystemTime();
            case APPLICATION:
                return nanoTime();
        }
        return 0L;
    }

    /**
     * Start the the time watch
     */
    public void start() {
        this.startTime = getTime();
        this.running = true;
    }

    /**
     * Stop the time watch
     */
    public void stop() {
        this.stopTime = getTime();
        this.running = false;
    }

    /**
     * Reset the watch starting the counter from 0
     */
    public void reset() {
        if (running) {
            this.startTime = getTime();
        }
        else {
            this.startTime = 0L;
            this.stopTime = 0L;
        }
    }

    /*
     * Return the elapsed time, scaled by a divisor
     */
    private long getElapsed(long divisor) {
        long elapsed;

        if(divisor == 0L){
            throw new IllegalArgumentException("Division by Zero, that could be a problem...");
        }

        if (running) {
            elapsed = ((getTime() - startTime));
        } else {
            elapsed = ((stopTime - startTime));
        }
        return divisor == 1L ? elapsed : elapsed/divisor;

    }

    /**
     * Returns the elapsed time in nanoseconds
     * @return elapsed time nanoseconds
     */
    public long getElapsedTime() {
        return getElapsed(1L);
    }

    /**
     * Returns the elapsed time in milliseconds
     * @return elapsed time milliseconds
     */
    public long getElapsedTimeMillis() {
        return getElapsed(1000000L);
    }

    /**
     * Returns the elapsed time in seconds
     * @return elapsed time Seconds
     */
    public long getElapsedTimeSecs() {
        return getElapsed(1000000000L);
    }

}
