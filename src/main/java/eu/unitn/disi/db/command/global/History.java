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
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Show a history of commands with the possibility of re-execute them
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@ConsoleCommand(
        name = "hist"
)
public class History extends Command {
    private int numEntries;
    private boolean repetitions;
    
    @Override
    protected void execute() throws ExecutionException {
        ExecutionService global = ExecutionService.getInstance();
        String line; 
        int entry; 
        List<? extends Pair<String,String[]>> history; 
        
        if (repetitions) {
            history = global.history;
        } else {
            LinkedHashSet<HashedPair<String, String[]>> tmp = new LinkedHashSet<>();
            Pair<String,String[]> cmd; 
            for (int i = 0; i < numEntries && i < global.history.size(); i++) {
                cmd = global.history.get(i);
                tmp.add(new HashedPair<>(cmd.getFirst(), cmd.getSecond()));
            }
            history = new ArrayList<>(tmp);
        }
        showHistory(global, history);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(global.in()));
            global.out().print("Choose one of the commands to execute again or 'q' to exit: ");
            while(!"q".equals(line = in.readLine())) {
                if (line != null && !"".equals(line)) {
                    try {
                        entry = Integer.parseInt(line);
                        if (entry > numEntries || entry > history.size()) {
                            error("Invalid entry, please type a valid one");
                        } else {
                            Object retval = global.runCommand(history.get(history.size() - entry).getSecond(), true); 
                            if (retval == ExecutionService.CommandError.ERROR) {
                                throw new ExecutionException("Execution error on calling command: %s", history.get(history.size() - entry).getFirst()); 
                            }
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        error("Insert a valid number or 'q' to exit");
                    }
                }
            }
        } catch (IOException ex) {
                
        }
    }

    @Override
    protected String commandDescription() {
        return "Show a history of the commands performed so far, allows relaunch of the same command";
    }
    
    @CommandInput(
            consoleFormat = "-n",
            defaultValue = "10", 
            description = "Number of entries to show", 
            mandatory = false
    )
    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    @CommandInput(
            consoleFormat = "-r",
            defaultValue = "false", 
            description = "Allow repetitions in the history", 
            mandatory = false
    )
    public void setRepetitions(boolean repetitions) {
        this.repetitions = repetitions;
    }
    
    
    private void showHistory(ExecutionService global, List<? extends Pair<String,String[]>> history) { 
        for (int i = 0; i < history.size() && i < numEntries; i++) {
            global.out().printf("[%d] %s\n", i + 1, history.get(history.size() - i - 1).getFirst());
        }
    }
    
    private class HashedPair<A,B> extends Pair<A,B> {

        public HashedPair(A first, B second) {
            super(first, second);
        }
        
        @Override
        public int hashCode() {
            return first.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof Pair && first != null) {
                return first.equals(((Pair)obj).getFirst());
            }
            return super.equals(obj);
        }
    }

}
