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
package eu.unitn.disi.db.command.exceptions;

/**
 * Represent an exception thrown with an executed command
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class ExecutionException extends GenericException {

    private static final long serialVersionUID = 1L;

    public ExecutionException(String message, Object... parameters) {
        super(message, parameters);
    }

    public ExecutionException(String formatString, Throwable cause, Object... parameters) {
        super(formatString, cause, parameters);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
