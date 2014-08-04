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
package eu.unitn.disi.db.command;

import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.util.JarLoader;
import eu.unitn.disi.db.command.util.Tokenizer;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;
import org.xeustechnologies.jcl.exception.JclException;

/**
 * This class wraps a console to be used. 
 * It cannot exists more than one console per process.
 * 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class ConsoleHandler {
    private static final String WELCOME_MESSAGE = 
            "Copyright (C) 2013 Davide Mottin <mottin@disi.unitn.eu>\nDCMD-v0.1\n _____     ______     __    __     _____    \n/\\  __-.  /\\  ___\\   /\\ \"-./  \\   /\\  __-.  \n\\ \\ \\/\\ \\ \\ \\ \\____  \\ \\ \\-./\\ \\  \\ \\ \\/\\ \\ \n \\ \\____-  \\ \\_____\\  \\ \\_\\ \\ \\_\\  \\ \\____- \n  \\/____/   \\/_____/   \\/_/  \\/_/   \\/____/ \n\n\nWELCOME to the Dynamic CoMmanD console, type the name of the \ncommand and the parameters. \n\nIf you don't remember the syntax of a parameter just type 'help [command]'\n\nEnjoy the experience! \n                                \n\nThis program is free software; you can redistribute it and/or\nmodify it under the terms of the GNU General Public License\nas published by the Free Software Foundation; either version 2\nof the License, or (at your option) any later version.\n\nThis program is distributed in the hope that it will be useful,\nbut WITHOUT ANY WARRANTY; without even the implied warranty of\nMERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\nGNU General Public License for more details.\n\nYou should have received a copy of the GNU General Public License\nalong with this program; if not, write to the Free Software\nFoundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.";
    private static final String CONSOLE_LINE = "dcmd> ";
    private static final String DEFAULT_PACKAGE = (System.getProperty("COMMANDS") != null? System.getProperty("COMMANDS") : "eu.unitn.disi.db.knowledge.commands") + ".";
    private PrintStream out = System.out;
    private static final int PAR_SIZE = 256;
    
    private static Logger logger = Logger.getLogger(CommandRunner.class);    
    private Map<String, Object> dynamicObjects;
    private JclObjectFactory factory = JclObjectFactory.getInstance();
    private JarClassLoader jcl;
    private Command lastCommand = null; 
    
    private static class Singleton {
        private static final ConsoleHandler INSTANCE = new ConsoleHandler();
    }
    
    private class ExecCommand implements Callable<Object> {
        private final String command;
        private final String[] params;
        
        public ExecCommand(String command, String[] params) {
            this.command = command;
            this.params = params;
        }
        
        @Override
        public Object call() throws Exception {
            try {
                Command c = (Command) factory.create(jcl, command);
                lastCommand = c;
                if (params != null) {
                    c.exec(params, dynamicObjects);
                }
                else {
                    c.exec(new String[]{}, dynamicObjects);
                }
                out.printf("Command %s terminated\n", command);
                if (c instanceof LoaderCommand) {
                    return ((LoaderCommand)c).getObject();
                }
            } catch (ExecutionException ex) {
                out.println("Command " + command + " has thrown an exception, message: " + ex.getMessage());
                logger.log(Level.FATAL, ex);
            } catch (NullPointerException cnfex) {
                out.println("Hey, command " + command + " doesn't exist, try again ;-)");
            } catch (JclException cnfex) {
                out.println("Hey, command " + command + " doesn't exist, try again ;-)");
            } catch (Exception ex)  {
                out.println("Some other problem occurred on command call, message: " + ex.getMessage());
                logger.log(Level.FATAL, null, ex);
            }
            
            return new Object();
        }
        
    }
   
    
    private ConsoleHandler() {
        dynamicObjects = new LinkedHashMap<String, Object>();
        jcl = new JarClassLoader();
    }

    public static ConsoleHandler getInstance() {
        return Singleton.INSTANCE;
    }
   
    
    private void stopCommand(String command) throws Exception {
    }
    
    private Object callCommand(String command, String[] params) throws Exception {
        try {
            Command c = (Command) factory.create(jcl, command);
            lastCommand = c;
            if (params != null) {
                c.exec(params, dynamicObjects);
            }
            else {
                c.exec(new String[]{}, dynamicObjects);
            }
            out.printf("Command %s terminated\n", command);
            if (c instanceof LoaderCommand) {
                return ((LoaderCommand)c).getObject();
            }
        } catch (ExecutionException ex) {
            out.println("Command " + command + " has thrown an exception, message: " + ex.getMessage());
            logger.log(Level.FATAL, ex);
        } catch (NullPointerException cnfex) {
            out.println("Hey, command " + command + " doesn't exist, try again ;-)");
        } catch (JclException cnfex) {
            out.println("Command " + command + " thrown an exception: " + cnfex.getMessage());
            logger.log(Level.FATAL, cnfex);
        } catch (Exception ex)  {
            out.println("Some other problem occurred on command call, message: " + ex.getMessage());
            logger.log(Level.FATAL, null, ex);
        }

        return new Object();
    }
    
    public void runConsole() {
        String line;
        CharSequence value; 
        Scanner in = new Scanner(System.in);        
        Tokenizer tok;
        //String libDir = null;
        CharSequence[] tokenized;  
        String[] splittedLine, params;
        int countTokens;
        String command;
        String mainCommand;
        String commandPackage = DEFAULT_PACKAGE; 
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Future<Object> cmd = null;
        
        out.println(WELCOME_MESSAGE);
        out.println();
        out.print(CONSOLE_LINE);
        //TODO: add SIGINT handling with Signal.handle() ...
        
        try {
            line = in.nextLine().trim();
            while (!"exit".equals(line)) {
                tok = new Tokenizer(line);
                tokenized = new String[PAR_SIZE];
                countTokens = 0;

                tok.next();
                while ((value = tok.value()) != null) {
                    tokenized[countTokens] = value;
                    //out.println("Found: " + tokenized[countTokens]);
                    tok.next();
                    countTokens++;
                }
                splittedLine = new String[countTokens];
                System.arraycopy(tokenized, 0, splittedLine, 0, countTokens);

                if (splittedLine.length > 0) {
                    mainCommand = splittedLine[0];
                    if ("help".equals(mainCommand)) {
                        if (splittedLine.length == 1) {
                            out.println("General help for DCMD console\n");
                            out.println("help [command]\n\tshow the help for a specific command or this help if no command provided");
                            out.println("pkg PACKAGE\n\tset the package for the commands");
                            out.println("jar JARFILE -lib LIBDIR\n\tload the jar file with the commands and the optiona libraries from LIBDIR directory");
                            out.println("obj $VARIABLE LOADER [params]\n\tload or unload (if option -d $VARIABLE is present) an object into $variable using the specific loader");
                            out.println("exec COMMAND [params]\n\texecute COMMAND with the specific parameters");
                            out.println("pwd\n\tprint the current directory");
                        } else {
                            command = commandPackage + splittedLine[1];
                            out.println("HELP for command " + splittedLine[1]);
                            try {
                                Command c = (Command) factory.create(jcl, command);//-
                                out.println(c.help());
                            } catch(UnsupportedOperationException ex) {
                                out.println("No help for this command");
                            } catch (NullPointerException cnfex) {
                                out.println("Hey, command " + splittedLine[1] + " doesn't exist, try again ;-)");
                            } catch (JclException cnfex) {
                                out.println("Hey, command " + splittedLine[1] + " doesn't exist, try again ;-)");
                            } catch (Exception ex)  {
                                out.println("Some other problem occurred on command call, message: " + ex.getMessage());
                                //ex.printStackTrace();
                                logger.log(Level.FATAL, null, ex);
                            }
                        }
                    }            
                    else if ("jar".equals(mainCommand)) {
                        if (splittedLine.length >= 2) {
                            jcl = new JarClassLoader();
                            if (splittedLine.length == 4 && "-lib".equals(splittedLine[2])) {
                                //libDir = splittedLine[3];
                                File libDir = new File(splittedLine[3]);
                                if (libDir.isDirectory() && libDir.canRead()) {
                                    File[] files = libDir.listFiles(new FileFilter() {
                                        @Override
                                        public boolean accept(File pathname) {
                                            if (pathname.getName().endsWith(".jar"))
                                                return true;
                                            return false;
                                        }
                                    });
                                    for (int i = 0; i < files.length; i++) {
                                        JarLoader.addFile(files[i]);
                                        out.printf("Successfully loaded library %s\n", files[i].getName());
                                    }
                                } else {
                                    out.printf("-lib parameter requires a readable directory\n");
                                }
                            }
                            try {
                                jcl.add(new FileInputStream(splittedLine[1])); 
                                
//                                DefaultContextLoader context = new DefaultContextLoader(jcl);
//                                context.loadContext();
                                out.printf("Successfully loaded jar %s\n", splittedLine[1]);
                            } catch (Exception ex) {
                                out.printf("Cannot load jar %s\n", splittedLine[1]);
                                logger.fatal("", ex);
                            }
                        } else {
                            out.println("Wrong number of parameters for jar command");
                        }
                    }
                    else if ("obj".equals(mainCommand)) {
                        //load GraphLoader freebase-graph.sin into a variable
                        if (splittedLine.length == 1) {
                            if (dynamicObjects.isEmpty()) {
                                out.println("No variables stored");
                            } else {
                                out.println("List of stored variables:");
                               for (String var : dynamicObjects.keySet()) {
                                   out.println(var + "->" + dynamicObjects.get(var));
                               }
                            }
                        } else if (splittedLine.length < 3) {
                            out.println("Wrong number of parameters for obj command, see the help");
                        } else {
                            if ("-d".equals(splittedLine[1])) {
                                if (splittedLine.length != 3) {
                                    out.println("Wrong number of parameters for obj command, see the help");
                                } else {
                                    Object o;
                                    if ((o = dynamicObjects.remove(splittedLine[2])) == null) {
                                        out.printf("Object %s is not a dynamic object\n", splittedLine[2]);
                                    } 
                                    else {
                                        out.printf("Object %s deleted from memory\n", splittedLine[2]);
                                        o = null;
                                    }
                                }
                            } else {                            
                                command = commandPackage + splittedLine[2];
                                params = null;
                                if (splittedLine.length > 2) {
                                    params = Arrays.copyOfRange(splittedLine, 3, splittedLine.length);
                                }
                                dynamicObjects.put(splittedLine[1], callCommand(command, params));
                            }
                        }
                    }
                    else if ("pkg".equals(mainCommand)) {
                        commandPackage = splittedLine[1] + ".";
                        out.printf("Command package succesfully set to %s\n", splittedLine[1]);
                    }
                    else if ("exec".equals(mainCommand)) {
                        if (splittedLine.length < 2) {
                            out.println("Wrong number of parameters for exec command, see the help");
                        } else {
                            command = commandPackage + splittedLine[1];
                            params = null;
                            if (splittedLine.length > 2) {
                                params = Arrays.copyOfRange(splittedLine, 2, splittedLine.length);
                            }
                            callCommand(command, params);
                            //cmd = pool.submit(new ExecCommand(command, params));
                        }                        
                    } 
                    else if ("kill".equals(mainCommand)) {
                        out.println("Trying to kill the command");
                        if (lastCommand != null) {
                            out.println("Command stopped");
                            lastCommand.stop();
                        }
                    }
                    else if ("pwd".equals(mainCommand)) {
                        out.println(System.getProperty("user.dir"));
                    }
                    else {
                        out.println("Come on, this command does not exist.");
                    }
                    //out.println(Arrays.toString(splittedLine));
                } //END IF 
                out.print(CONSOLE_LINE);
                line = in.nextLine().trim();
            } //END WHILE
            out.println("Bye, I'll miss you");
        } catch (Exception ex) {
            logger.fatal("Some unexpected error happened that prevented the normal execution of the program", ex);
        }
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }
    
}
