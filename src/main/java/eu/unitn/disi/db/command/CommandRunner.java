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
package eu.unitn.disi.db.command;

import eu.unitn.disi.db.command.exceptions.ExecutionException;
import java.io.PrintStream;
import java.util.Arrays;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This is the main engine, it executes commands.
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class CommandRunner {
    private static final PrintStream out = System.out;
    private static final String DEFAULT_PACKAGE = (System.getProperty("COMMANDS") != null? System.getProperty("COMMANDS") : "eu.unitn.disi.db.knowledge.commands") + ".";
    
    private static Logger logger = Logger.getLogger(CommandRunner.class);

    public static void run(String[] args) {
        String command;

        if (args.length == 0) {
            out.println("No command specified. The right syntax is:\n" +
                               "[jar_name] [command] [parameters]\n" +
                               "where [command] is a fully qualified command name\n" +
                               "To print the help for a command, type: help [command]");
        } else if ("help".equals(args[0])) {
            if (args.length == 1) {
                out.println("You must specify a command name");
            } else {
                command = DEFAULT_PACKAGE + args[1];
                out.println("HELP for command "  + args[1]);
                try {
                    Command c = (Command)Class.forName(command).newInstance();
                    out.println(c.help());
                } catch(UnsupportedOperationException ex) {
                    out.println("No help for this command");
                } catch (ClassNotFoundException cnfex) {
                    out.println("Hey, command " + args[1] + " doesn't exist, try again ;-)");
                } catch (Exception ex)  {
                    out.println("Some other problem occurred on command call, message: " + ex.getMessage());
                    //ex.printStackTrace();
                    logger.log(Level.FATAL, null, ex);
                }
            }
        } else {
            command = DEFAULT_PACKAGE + args[0];
            try {
                Command c = (Command)Class.forName(command).newInstance();
                if (args.length > 1) {
                    c.exec(Arrays.copyOfRange(args, 1, args.length));
                }
                else {
                    c.exec(new String[]{});
                }
            } catch (ExecutionException ex) {
                out.println("Command " + args[0] + " has thrown an exception, message: " + ex.getMessage());
                //ex.printStackTrace();
                logger.log(Level.FATAL, ex);
            } catch (ClassNotFoundException cnfex) {
                out.println("Hey, command " + args[0] + " doesn't exist, try again ;-)");
            } catch (Exception ex)  {
                out.println("Some other problem occurred on command call, message: " + ex.getMessage());
                logger.log(Level.FATAL, null, ex);
            }
        }
    }


}
