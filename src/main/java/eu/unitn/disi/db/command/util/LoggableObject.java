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

import static java.lang.String.format;
import org.apache.log4j.Level;
import static org.apache.log4j.Level.DEBUG;
import static org.apache.log4j.Level.ERROR;
import static org.apache.log4j.Level.FATAL;
import static org.apache.log4j.Level.INFO;
import static org.apache.log4j.Level.WARN;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;


/**
 * This class abstracts the concept of object with logging capabilities. 
 * Should be inherited by all the classes in the framework
 * 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class LoggableObject {
   
    protected Logger logger = getLogger(this.getClass().getCanonicalName());
    
    /*
     * Logging methods (wrappers of log4 with format string facilities)
     */
    protected void debug(String message, Object... args) {
        log(DEBUG, null, message, args);
    }
    
    protected void warn(String message, Object... args) {
        log(WARN, null, message, args);
    }

    protected void fatal(String message, Object... args) {
        log(FATAL, null, message, args);
    }
    
    protected void error(String message, Object... args) {
        log(ERROR, null, message, args);
    }

    protected void info(String message, Object... args) {
        log(INFO, null, message, args);
    }

    protected void debug(String message, Throwable ex, Object... args) {
        log(DEBUG, ex, message, args);
    }
    
    protected void warn(String message, Throwable ex, Object... args) {
        log(WARN, ex, message, args);
    }

    protected void fatal(String message, Throwable ex, Object... args) {
        log(FATAL, ex, message, args);
    }
    
    protected void error(String message, Throwable ex, Object... args) {
        log(ERROR, ex, message, args);
    }

    protected void info(String message, Throwable ex, Object... args) {
        log(INFO, ex, message, args);
    }
    
    protected void log(Level level, String message, Object... args) {
        log(level, null, message, args);
    }

    protected void log(Level level, Throwable ex, String message, Object... args) {
        logger.log(level, format(message, args), ex);
    }
}
