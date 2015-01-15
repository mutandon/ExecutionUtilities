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
import eu.unitn.disi.db.command.DynamicInput;
import eu.unitn.disi.db.command.PositionalInput;
import eu.unitn.disi.db.command.exceptions.ArgumentDeclarationException;
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.exceptions.WrongParameterException;
import eu.unitn.disi.db.command.util.LoggableObject;
import eu.unitn.disi.db.command.util.Pair;
import eu.unitn.disi.db.command.util.StringUtils;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import static java.util.Arrays.copyOfRange;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.xeustechnologies.jcl.JarClassLoader;
import org.xeustechnologies.jcl.JclObjectFactory;

/**
 * This class keeps the status of the system in terms of commands and global
 * variables. It is mainly used in the console.
 *
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
final class ExecutionService extends LoggableObject {

    private final Map<String, Class<? extends Command>> loadedCommands;
    private final Map<String, Class<? extends Command>> consoleCommands;
    private final Map<String, Object> dynamicObjects;
    final List<Pair<String, String[]>> history;
    JarClassLoader commandLoader; 
    private PrintStream out = System.out;
    private InputStream in = System.in; 
    private static final String EMPTY_COMMAND = "                     ";
    private static final String NOT_EXISTING_ERROR = "Hey, command '%s' doesn't exist, try again ;-)";
    public static final String COMMAND_SEPARATOR = "=%=";
    public static final String BATCH_COMMENT = "#";
    
    public enum CommandError {
        ERROR
    }
    
    
    private ExecutionService() {
        loadedCommands = new HashMap<>();
        dynamicObjects = new HashMap<>();
        consoleCommands = new HashMap<>();
        history = new ArrayList<>();
        
        Reflections reflections = new Reflections("", new SubTypesScanner(false));
        ConsoleCommand consoleCmdAnn;

        for (Class<? extends Command> cmd : reflections.getSubTypesOf(Command.class)) {
            if (!Modifier.isAbstract(cmd.getModifiers())) {
                if (cmd.isAnnotationPresent(ConsoleCommand.class)) {
                    consoleCmdAnn = cmd.getAnnotation(ConsoleCommand.class);
                    try {
                        addCommand(consoleCmdAnn.name(), cmd, consoleCommands);
                    } catch (ArgumentDeclarationException ex) {
                        fatal("%s", ex.getMessage());
                    }
                } else {
                    try {
                        addExecutableCommand(cmd.getSimpleName().toLowerCase(), cmd);
                    } catch (ArgumentDeclarationException ex) {
                        fatal("%s", ex.getMessage());
                    }
                }
            }
        }
    }

    private static class Singleton {
        private static final ExecutionService INSTANCE = new ExecutionService();
    }

    public static ExecutionService getInstance() {
        return Singleton.INSTANCE;
    }

    public void addExecutableCommand(String name, Class<? extends Command> c)
            throws ArgumentDeclarationException {
        addCommand(name.toLowerCase(), c, loadedCommands);
    }

    private void addCommand(String name, Class<? extends Command> c, Map<String, Class<? extends Command>> commands)
            throws ArgumentDeclarationException {
        checkCommandCorrectness(c);
        commands.put(name.toLowerCase(), c);
    }

    public void removeExecutableCommand(String name) {
        loadedCommands.remove(name);
    }

    public void clearCommands() {
        loadedCommands.clear();
    }
    
    public void clearHistory() {
        history.clear();
    }

    public Map<String, Object> getDynamicObjects() {
        return dynamicObjects;
    }

    public PrintStream out() {
        return out;
    }
    
    public InputStream in() {
        return in; 
    }

    public Object runCommand(String[] args) {
        return runCommand(args, false);
    }

    public Object runCommand(String[] args, boolean console) {
        return runCommand(args, dynamicObjects, console);
    }

    public Object runCommand(String[] args, Map<String, Object> dynamicObjects, boolean console) {
        Object obj = new Object();
        String[] decodedArgs; 
        Command c;
        try {
            if (console) {
                decodedArgs = new String[args.length];
                for (int i = 0; i < decodedArgs.length; i++) {
                    decodedArgs[i] = args[i].replace(COMMAND_SEPARATOR, ""); 
                }
                c = (Command) consoleCommands.get(args[0].toLowerCase()).newInstance();
                history.add(new Pair<>(StringUtils.join(decodedArgs, " "), args));
            } else {
                JclObjectFactory commandFactory = JclObjectFactory.getInstance();
                c = (Command) commandFactory.create(commandLoader, loadedCommands.get(args[0].toLowerCase()).getName());            
            }
            if (args.length > 1) {
                c.exec(copyOfRange(args, 1, args.length), dynamicObjects);
            } else {
                c.exec(new String[]{}, dynamicObjects);
            }
            if (c instanceof LoaderCommand) {
                obj = ((LoaderCommand) c).getObject();
            }
        } catch (WrongParameterException ex) {
            error("Wrong parameter: %s", ex.getMessage());
            obj = CommandError.ERROR;
        } catch (ExecutionException ex) {
            fatal("Command %s has thrown an exception, message: %s", ex, args[0], ex.getMessage());
            obj = CommandError.ERROR;
        } catch (NullPointerException cnfex) {
            error(NOT_EXISTING_ERROR, args[0]);
            obj = CommandError.ERROR;
        } catch (Exception ex) {
            fatal("Some other problem occurred on command call, message: %s ", ex, ex.getMessage());
            obj = CommandError.ERROR;
        }
        return obj;
    }

    public void printHelp(String cmd, String startMessage, boolean console) {
        Map<String, Class<? extends Command>> commands;
        if (console) {
            commands = consoleCommands;
        } else {
            commands = loadedCommands;
        }
        try {
            if (!commands.isEmpty()) {
                if (cmd == null || "".equals(cmd)) {
                    out.print(startMessage);
                    for (String cmd1 : commands.keySet()) {
                        out.println("  " + (cmd1.length() < EMPTY_COMMAND.length() ? cmd1.concat(EMPTY_COMMAND.substring(0, EMPTY_COMMAND.length() - cmd1.length())) : cmd1 + "  ") + commands.get(cmd1).newInstance().commandDescription());
                    }
                } else {
                    out.println("HELP for command " + cmd);
                    Command c = (Command) commands.get(cmd).newInstance();
                    out.println(c.help());
                }
            } else {
                out.println("No command loaded");
            }
        } catch (UnsupportedOperationException ex) {
            info("No help for this command");
        } catch (NullPointerException cnfex) {
            error(NOT_EXISTING_ERROR, cmd);
        } catch (Exception ex) {
            fatal("Some other problem occurred on command call, message: %s", ex, ex.getMessage());
        }
    }

    public void printHelp(String cmd) {
        printHelp(cmd, "The following commands are available\n", false);
    }

    public void checkCommandCorrectness(Class<? extends Command> command)
            throws ArgumentDeclarationException {
        Method[] methods = command.getMethods();
        Annotation[] annotations;
        Set<Integer> positions = new HashSet<>();
        Set<String> names = new HashSet<>();
        String name;
        for (Method method : methods) {
            annotations = method.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof CommandInput || annotation instanceof DynamicInput) {
                    if (!checkAnnotatedMethod(method)) {
                        throw new ArgumentDeclarationException("Annotated method %s in command %s has more than one parameter!", method.getName(), command.getName());
                    }
                    name = annotation instanceof CommandInput ? ((CommandInput) annotation).consoleFormat() : ((DynamicInput) annotation).consoleFormat();
                    if (names.contains(name)) {
                        throw new ArgumentDeclarationException("Annotation with name %s on method %s in command %s is repeated", name, method.getName(), method.getDeclaredAnnotations());
                    }
                    names.add(name);
                } else if (annotation instanceof PositionalInput) {
                    if (!checkAnnotatedMethod(method)) {
                        throw new ArgumentDeclarationException("Annotated method %s in command %s has more than one parameter!", method.getName(), command.getName());
                    }
                    positions.add(((PositionalInput) annotation).position());
                }
            }
        }
        if (!checkPositions(positions)) {
            throw new ArgumentDeclarationException("Positional parameters in command %s should provide consecutive positions and numbered from 1", command.getName());
        }
    }

    private static boolean checkPositions(Collection<Integer> positions) {
        for (int i = 0; i < positions.size(); i++) {
            if (!positions.contains(i + 1)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkAnnotatedMethod(Method m) {
        return m.getParameterTypes().length == 1;
    }

}
