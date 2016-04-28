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

import static java.lang.String.format;
import java.util.Formatter;

/**
 * This class represents exceptions that can use format string as input. This class
 * has not empty constructor.
 * @see Formatter
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public abstract class FormattedException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public FormattedException(Throwable cause) {
        super(cause);
    }

    public FormattedException(String formatString, Throwable cause, Object... parameters) {
        super(format(formatString, parameters), cause);
    }

    public FormattedException(String message, Object... parameters) {
        super(format(message, parameters));
    }
}
