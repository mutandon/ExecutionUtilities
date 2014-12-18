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

import java.lang.management.ManagementFactory;
import static java.lang.management.ManagementFactory.getThreadMXBean;
import java.lang.management.ThreadMXBean;

/**
 * Static methods to return CPU time in multithreaded or single threaded 
 * environments
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class Time {

    /**
     * Get the CPU time in nanoseconds
     * @return CPU time
     */
    public static long getCpuTime() {
        ThreadMXBean bean = getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
                ? bean.getCurrentThreadCpuTime() : 0L;
    }

    /** 
     * Get user time in nanoseconds. 
     * @return User time
     */
    public static long getUserTime() {
        ThreadMXBean bean = getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
                ? bean.getCurrentThreadUserTime() : 0L;
    }

    /** 
     * Get system time in nanoseconds. 
     * @return System time
     */
    public static long getSystemTime() {
        ThreadMXBean bean = getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
                ? (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime()) : 0L;
    }

    /** 
     * Get CPU time in nanoseconds for a particular set of thread ids
     */
    public static long getCpuTime(long[] ids) {
        ThreadMXBean bean = getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported()) {
            return 0L;
        }
        long time = 0L;
        for (int i = 0; i < ids.length; i++) {
            long t = bean.getThreadCpuTime(ids[i]);
            if (t != -1) {
                time += t;
            }
        }
        return time;
    }

    /** 
     * Get user time in nanoseconds for a particular set of thread ids
     */
    public static long getUserTime(long[] ids) {
        ThreadMXBean bean = getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported()) {
            return 0L;
        }
        long time = 0L;
        for (int i = 0; i < ids.length; i++) {
            long t = bean.getThreadUserTime(ids[i]);
            if (t != -1) {
                time += t;
            }
        }
        return time;
    }

    /** 
     * Get system time in nanoseconds for a particular set of thread ids 
     */
    public static long getSystemTime(long[] ids) {
        ThreadMXBean bean = getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported()) {
            return 0L;
        }
        long time = 0L;
        for (int i = 0; i < ids.length; i++) {
            long tc = bean.getThreadCpuTime(ids[i]);
            long tu = bean.getThreadUserTime(ids[i]);
            if (tc != -1 && tu != -1) {
                time += (tc - tu);
            }
        }
        return time;
    }
}
