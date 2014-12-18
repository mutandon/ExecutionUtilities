/*
 * Copyright (C) 2014 Davide Mottin <mottin@disi.unitn.eu>
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
 * The arguments are declared in a bad way, the source code is incorrect. 
 * Although it should be at compile time, that check is hard to perform, so we
 * raise an exception on the formal correctness before the actual execution of 
 * a command. 
 * 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class ArgumentDeclarationException extends GenericException {

    public ArgumentDeclarationException(String message, Object... parameters) {
        super(message, parameters);
    }

    public ArgumentDeclarationException(String formatString, Throwable cause, Object... parameters) {
        super(formatString, cause, parameters);
    }

    public ArgumentDeclarationException(Throwable cause) {
        super(cause);
    }
}
