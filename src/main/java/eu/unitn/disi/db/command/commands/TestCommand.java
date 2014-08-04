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
package eu.unitn.disi.db.command.commands;

import eu.unitn.disi.db.command.Command;
import eu.unitn.disi.db.command.CommandInput;
import eu.unitn.disi.db.command.ParametersNumber;
import eu.unitn.disi.db.command.exceptions.ExecutionException;

/**
 * A test command to 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public class TestCommand extends Command {
    private String text; 
    
    
    @Override
    protected void execute() throws ExecutionException {
        System.out.printf("Hello %s, this is your first command!\n", text);
    }
    
    @Override
    protected String commandDescription() {
        return "A test command ;-)";
    }
    
    @CommandInput(
        consoleFormat="-t",
        defaultValue="",
        mandatory=true,
        description="Text to be printed",
        parameters= ParametersNumber.TWO
    )
    public void setText(String text) {
        this.text = text;
    }
}
