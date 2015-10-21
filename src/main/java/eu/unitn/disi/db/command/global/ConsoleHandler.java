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
package eu.unitn.disi.db.command.global;

import eu.unitn.disi.db.command.global.ExecutionService.CommandError;
import eu.unitn.disi.db.mutilities.Pair;
import eu.unitn.disi.db.command.util.Tokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Scanner;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;

/**
 * This class wraps a console to be used. It cannot exists more than one console
 * per process.
 *
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class ConsoleHandler {

    private final String WELCOME_MESSAGE;
    private final String EXIT_MESSAGE;
    private static final String CONSOLE_LINE = "dcmd> ";
    private static final Logger logger = getLogger(ConsoleHandler.class);

    private static class Singleton {

        private static final ConsoleHandler INSTANCE = new ConsoleHandler();
    }

    private ConsoleHandler() {
        //dynamicObjects = new LinkedHashMap<>();
        String line;
        StringBuilder builder = new StringBuilder();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(ConsoleHandler.class.getResourceAsStream("/console.welcome")))) {
            while ((line = input.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException ex) {
            logger.fatal("Cannot find the welcome message in the jar, check the integrity of the package");

        }
        WELCOME_MESSAGE = builder.toString();
        builder = new StringBuilder();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(ConsoleHandler.class.getResourceAsStream("/console.exit")))) {
            while ((line = input.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } catch (IOException ex) {
            logger.fatal("Cannot find the exit message in the jar, check the integrity of the package");

        }
        EXIT_MESSAGE = builder.toString();

    }

    public static ConsoleHandler getInstance() {
        return Singleton.INSTANCE;
    }


    public void runConsole() {
        ExecutionService global = ExecutionService.getInstance();
        PrintStream out = global.out();
        String line;
        Scanner in = new Scanner(System.in);
        Tokenizer tok;
        String[] tokenizedCommand, params;
        Object retval = null; 
        String mainCommand;
        out.println(WELCOME_MESSAGE);
        out.println();
        out.print(CONSOLE_LINE);

        try {
            line = in.nextLine().trim();
            while (!"exit".equals(line) && !"quit".equals(line)) {
                tokenizedCommand = ExecutionService.tokenizeCommand(line);
                if (tokenizedCommand.length > 0) {
                    mainCommand = tokenizedCommand[0];
                    
                    if (null != mainCommand) {
                        switch (mainCommand) {
                            case "\\?": //global help
                                global.printHelp("", "Available console commands\n", true);
                                break;                                   
                            default: 
                                retval = global.runCommand(tokenizedCommand, true);
                        }
                        if (retval == null || !(retval instanceof CommandError) || CommandError.NOT_EXISTS != (CommandError)retval) {
                            if (!"hist".equals(mainCommand.toLowerCase())) {
                                global.history.add(new Pair<>(line, tokenizedCommand));
                            }
                        }
                    }
                } //END IF 
                out.print(CONSOLE_LINE);
                line = in.nextLine().trim();
            } //END WHILE
            out.println(EXIT_MESSAGE);
            System.exit(0);
        } catch (Exception ex) {
            logger.fatal("Some unexpected error happened that prevented the normal execution of the program", ex);
        }
    }
}
