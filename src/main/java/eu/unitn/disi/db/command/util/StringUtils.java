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

package eu.unitn.disi.db.command.util;

import java.util.regex.Pattern;

/**
 * Utilities for strings
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public final class StringUtils {
    public static final String PUNCT_BLANK_SPLITTER = "[\\\\p{Space}\\\\p{Punct}]+";
    public static final Pattern PUNCT_BLANK_PATTERN = Pattern.compile(PUNCT_BLANK_SPLITTER);
    
    private StringUtils() {}

    /**
     * Collapse an array of strings into a single array using a separator character
     * @param array The array to be collapsed
     * @param separator The separator to use
     * @return The joined String
     */
    public static String join(String[] array, char separator) {
        return join(array, separator + "");
    }
    
    public static String join(String[] array, String separator) {
        StringBuilder sb = new StringBuilder(); 
        int i; 
        for (i = 0; i < array.length - 1; i++) {
            sb.append(array[i]).append(separator);
        }
        sb.append(array[i]);
        return sb.toString(); 
    }
    
    /**
     * Split the string using the separator. Since it might contain special characters
     * {@link Pattern.quote} is used instead
     * @param string The input string to be splitted
     * @param separator The separator string (may contian special characters)
     * @return The splitted string
     * @see Pattern
     */
    public static String[] split(String string, String separator) {
        String[] splittedString = string.split(Pattern.quote(separator)); 
        return splittedString;
    }
}
