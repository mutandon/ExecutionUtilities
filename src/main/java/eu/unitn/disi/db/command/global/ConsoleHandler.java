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

import static eu.unitn.disi.db.command.global.ExecutionService.COMMAND_SEPARATOR;
import static eu.unitn.disi.db.command.util.StringUtils.join;
import eu.unitn.disi.db.command.util.Tokenizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
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
    private static final String CONSOLE_LINE = "\bdcmd> ";
    private static final int PAR_SIZE = 256;
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
        CharSequence value;
        Scanner in = new Scanner(System.in);
        Tokenizer tok;
        CharSequence[] tokenized;
        String[] splittedLine, params;
        int countTokens;
        String mainCommand;
        out.println(WELCOME_MESSAGE);
        out.println();
        out.print(CONSOLE_LINE);

        try {
            line = in.nextLine().trim();
            while (!"exit".equals(line)) {
                tok = new Tokenizer(line);
                tokenized = new String[PAR_SIZE];
                countTokens = 0;

                tok.next();
                while ((value = tok.value()) != null) {
                    tokenized[countTokens] = value;
                    tok.next();
                    countTokens++;
                }
                splittedLine = new String[countTokens];
                System.arraycopy(tokenized, 0, splittedLine, 0, countTokens);
                if (splittedLine.length > 0) {
                    mainCommand = splittedLine[0];

                    if (null != mainCommand) {
                        switch (mainCommand) {
                            //COmmand execution is special
                            case "exec":
                            case "\\e":
                                if (splittedLine.length > 1) {
                                    global.runCommand(Arrays.copyOfRange(splittedLine, 1, splittedLine.length));
                                } else {
                                    global.printHelp("");
                                }
                                break;
                            case "obj": 
                            case "\\o":
                                if (splittedLine.length > 2) {
                                    global.runCommand(new String[]{mainCommand, splittedLine[1], join(Arrays.copyOfRange(splittedLine, 2, splittedLine.length),COMMAND_SEPARATOR)}, true);
                                } else {
                                    global.out().println("Insufficient parameters, usage: obj VARIABLE COMMAND");
                                }
                                break;                                
                            case "\\?": //global help
                                global.printHelp("", "Available console commands\n", true);
                                break;                                   
                            default: 
                                global.runCommand(splittedLine, true);
                        }
                    }
                } //END IF 
                out.print(CONSOLE_LINE);
                line = in.nextLine().trim();
            } //END WHILE
            out.println(EXIT_MESSAGE);
        } catch (Exception ex) {
            logger.fatal("Some unexpected error happened that prevented the normal execution of the program", ex);
        }
    }
}
