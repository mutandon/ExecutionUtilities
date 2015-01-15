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

import eu.unitn.disi.db.command.PositionalInput;
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.util.StringUtils;

/**
 * Command execution command
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@ConsoleCommand(name = "exec")
public class Exec extends Command {
    private String command; 
    
    @Override
    protected void execute() throws ExecutionException {
        ExecutionService global = ExecutionService.getInstance();
        Object retval = global.runCommand(StringUtils.split(command, ExecutionService.COMMAND_SEPARATOR)); 
        if (retval == ExecutionService.CommandError.ERROR) {
            throw new ExecutionException("Execution error on calling command: %s", command.replace(ExecutionService.COMMAND_SEPARATOR, " ")); 
        }
    }

    @Override
    protected String commandDescription() {
        return "Execute a user defined command (loaded from a jar)"; 
    }
    
    @PositionalInput(
        description = "the command to be executed", 
        name = "command", 
        position = 1
    )
    public void setCommand(String command) {
        this.command = command;
    }
}
