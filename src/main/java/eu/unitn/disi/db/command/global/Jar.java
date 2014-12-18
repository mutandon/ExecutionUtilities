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
package eu.unitn.disi.db.command.global;

import eu.unitn.disi.db.command.CommandInput;
import eu.unitn.disi.db.command.PositionalInput;
import eu.unitn.disi.db.command.exceptions.ArgumentDeclarationException;
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import static eu.unitn.disi.db.command.util.JarLoader.addFile;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.xeustechnologies.jcl.JarClassLoader;

/**
 *
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@ConsoleCommand(name = "jar")
public class Jar extends Command {
    private String[] libraryPaths;
    private String jarPath; 
    
    
    private static final JarFilter JAR_FILTER = new JarFilter(); 
    
    private static class JarFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith(".jar")) {
                return true;
            }
            return false;
        }
    }

    @Override
    protected void execute() throws ExecutionException {        
        ExecutionService global = ExecutionService.getInstance();
        PrintStream out = global.out();
        Map<String, Class<? extends Command>> commands; 
        LocalLoader loader;
                
        try {
            if (libraryPaths != null) {
                //libDir = splittedLine[3];
                for (String lib : libraryPaths) {
                    if (!"".equals(lib)) {
                        File libDir = new File(lib);
                        if (libDir.isDirectory() && libDir.canRead()) {
                            File[] files = libDir.listFiles(JAR_FILTER);
                            for (File file : files) {
                                addFile(file);
                                out.printf("Successfully loaded library %s\n", file.getName());
                            }
                        } else {
                            out.printf("-lib parameter requires a readable directory\n");
                        }
                    }
                }
            }
            global.commandLoader = new JarClassLoader(); 
            global.commandLoader.add(new FileInputStream(jarPath));
            loader = new LocalLoader(global.commandLoader.getLoadedResources());
            commands = loader.loadCommands();
            global.clearCommands();
            Set<String> className = commands.keySet();
            Class c;
            for (String cls : className) {
                c = commands.get(cls);
                global.addExecutableCommand(c.getSimpleName(), commands.get(cls));
            }
            out.printf("Successfully loaded jar %s\n", jarPath);
        } catch (IOException ex) {
            out.printf("Cannot load jar %s\n", jarPath);
            fatal("", ex);
        } catch (ArgumentDeclarationException ex) {
            fatal("%s", ex.getMessage());
        }
    }

    @Override
    protected String commandDescription() {
        return "Load a jar with commands and a set of libraries";
    }

    @CommandInput(
            consoleFormat = "-lib", 
            defaultValue = "", 
            mandatory = false, 
            description = "additional library directories to be loaded (comma separated)"
    )
    public void setLibraryPaths(String[] libraryPaths) {
        this.libraryPaths = libraryPaths;
    }

    @PositionalInput(
            description = "the complete path of the jar to be loaded", 
            name = "jarPath", 
            position = 1
    )
    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    
    private class LocalLoader extends ClassLoader {
        Map<String, byte[]> jarFiles; 
        
        public LocalLoader(Map<String, byte[]> jarFiles) {
            this.jarFiles = jarFiles;
        }

        public Map<String, Class<? extends Command>> loadCommands() {
            Map<String, Class<? extends Command>> commands = new HashMap<>(); 
            byte[] classBytes;
            String className, classPath; 
            Class c,sc;
            LinkedList<String> waiting = new LinkedList<>(jarFiles.keySet());
            while(waiting.isEmpty()) {
                classPath = waiting.poll();
                className = checkClass(classPath);
                //System.out.println(className);
                if (!"".equals(className)) {
                    try {
                        classBytes = jarFiles.get(classPath);
                        c = defineClass( className, classBytes, 0, classBytes.length ); 
                        sc = c; 
                        //Navigate up the class hierarchy till we find Command; 
                        while ((sc = sc.getSuperclass()) != null) {
                            if (sc == Command.class) {
                                commands.put(className, c);
                                break;
                            }
                        }
                    } catch (NoClassDefFoundError ex) {
                        waiting.add(classPath);
                    }
                }
            }
            return commands;
        }
        
        private String checkClass(String classPath) {
            String c = ""; 
            if (classPath.endsWith(".class")) {
                c = classPath.replace("/", "."); 
                c = c.substring(0, c.length() - 6);
            }
            return c; 
        }
        
    }
}
