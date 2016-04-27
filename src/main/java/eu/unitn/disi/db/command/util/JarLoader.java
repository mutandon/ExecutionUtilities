/*
 * Copyright (C) 2013 Davide Mottin <mottin@disi.unitn.eu>
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

import java.io.File;
import java.io.IOException;
import static java.lang.ClassLoader.getSystemClassLoader;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import static org.apache.log4j.Logger.getLogger;

/**
 * Jar loader used to load commands within jars dynamically
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class JarLoader {    
    
    private static final Class[] parameters = new Class[] {URL.class};
    
    private JarLoader() {
    }
    
    public static void addFile(String s) throws IOException
    {
        File f = new File(s);
        addFile(f);
    }

    public static void addFile(File f) throws IOException
    {
        //f.toURL is deprecated
        addURL(f.toURI().toURL());
    }
    
    public static void addURL(URL u) throws IOException
    {
        URLClassLoader sysloader = (URLClassLoader) getSystemClassLoader();
        Class sysclass = URLClassLoader.class;

        try {
            Method method = sysclass.getDeclaredMethod("addURL", parameters);
            method.setAccessible(true);
            method.invoke(sysloader, new Object[] {u});
        } catch (Throwable t) {
            getLogger(JarLoader.class.getClass()).fatal("Cannot load the jar", t);
            throw new IOException("Error, could not add URL to system classloader");
        }
    }    
}
