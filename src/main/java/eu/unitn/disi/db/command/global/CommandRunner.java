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
package eu.unitn.disi.db.command.global;

import static eu.unitn.disi.db.command.global.ExecutionService.getInstance;

/**
 * This is the main engine, it executes commands.
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public final class CommandRunner {
    private static final String NO_COMMAND_ERROR = "No command specified. The right syntax is:\n" +
                               "[jar_name] command [parameters]\n" +
                               "where 'command' is a fully qualified command name\n" +
                               "To print the help for a command, type: [jar_name] help command"; 

    private static final ExecutionService status = getInstance();
    
    public static void run(String[] args) {
        if (args.length == 0) {
            status.out().println(NO_COMMAND_ERROR);
        } else if ("help".equals(args[0])) {
            status.printHelp(args.length > 1 ? args[1] : "");
        } else {
            status.runCommand(args);
        }
    }
}
