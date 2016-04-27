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
import java.util.Map;

/**
 * Command to load (big) objects into memory and store into variables. 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@ConsoleCommand(name = "obj")
public class Obj extends Command {
    private String variable; 
    private String command; 
    
    @Override
    protected void execute() throws ExecutionException {
        ExecutionService global = ExecutionService.getInstance();
        Map<String, Object> dynamicObjects = global.getDynamicObjects();
        if (dynamicObjects.containsKey(variable)) {
            warn("Overriding an existing object");
        }
        Object retval = global.runCommand(ExecutionService.tokenizeCommand(command));
        if (retval instanceof ExecutionService.CommandError) {
            throw new ExecutionException("Execution error on calling command: %s", command); 
        } 
//        else  if (retval == ExecutionService.CommandError.NOT_EXISTS) {
//            throw new NullPointerException(String.format("Command does not exists: %s", command)); 
//        }
        dynamicObjects.put(variable, retval);
    }

    @Override
    protected String commandDescription() {
        return "Load an object into the console using the appropriate loader";
    }

    @PositionalInput(
        description = "the name of the variable to be stored and used (for convention better start with $[variablename]", 
        name = "variable", 
        position = 1
    )
    public void setVariable(String variable) {
        this.variable = variable;
    }

    @PositionalInput(
        description = "the loader command to be used", 
        name = "command", 
        position = 2
    )
    public void setCommand(String command) {
        this.command = command;
    }
}
