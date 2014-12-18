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
package eu.unitn.disi.db.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a positional input for parameters
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PositionalInput {
    /** 
     * The description of the parameter used for the help
     * @return The description of the command. 
     */
    public String description() default "";
    /**
     * Specify the position of the parameter. Positional parameters must be ordered
     * and consecutive. Repeated numbers or not consecutive will throw an exception. 
     * The numbering should start by 1 
     * @return The position of the parameter in the command. 
     */
    public int position();
    /**
     * Specify the name of the parameter to be visualized in the help. 
     * For positional arguments we do not bind this name to a paramater format, 
     * that is instead specified by the position. 
     * @return The console name of the parameter
     */
    public String name();
}
