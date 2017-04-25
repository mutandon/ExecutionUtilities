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
import eu.unitn.disi.db.command.PositionalInput;
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import static eu.unitn.disi.db.command.global.CommandRunner.QUITS;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Load and execute a batch of commands from file
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
@ConsoleCommand(
        name = "batch"
)
public class Batch extends Command {
    private String batchFile;
    private boolean stop; 
    
    @Override
    protected void execute() throws ExecutionException {
        Path p;
        List<String> lines; 
        String[] tokenizedCommand; 
        ExecutionService global = ExecutionService.getInstance();
        Object retval;
        p = FileSystems.getDefault().getPath(batchFile);
        
        
        try {
            if (batchFile.endsWith(".js")) {
                ScriptEngineManager manager = new ScriptEngineManager();
                ScriptEngine engine = manager.getEngineByExtension("js");
                
                try (FileReader reader = new FileReader(batchFile)) {
                    engine.eval(reader);
                    lines = (List<String>) engine.get("commands");
                }
            } else {
                lines = Files.readAllLines(p, Charset.defaultCharset());
            }
            for (String line : lines) {
                if (line != null) {
                    line = line.trim();
                    if(!QUITS.contains(line)){
                        break;
                    }
                    if (!"".equals(line) && !line.startsWith(ExecutionService.BATCH_COMMENT)) {
                        tokenizedCommand = ExecutionService.tokenizeCommand(line);
                        retval = global.runCommand(tokenizedCommand, true);
                        if (stop && retval instanceof ExecutionService.CommandError) {
                            break; 
                        }
                    }
                }
            }
        } catch (IOException ex) {
            error("Cannot read batch file %s", batchFile);
        } catch (ScriptException ex) {
            ex.printStackTrace();
            fatal("Javascript error in line: %d, column: %d, message: %s", ex.getLineNumber(), ex.getColumnNumber(), ex.getMessage());
        } catch (NullPointerException ex) {
            fatal("Malformed javascript: it must contain an ArrayList named 'commands' of string commands to be executed");
        }
    }
    
    @Override
    protected String commandDescription() {
        return "Load and execute a batch of commands from file";
    }

    @PositionalInput(
            name = "batchFile",
            description = "the input batch file",
            position = 1
    )
    public void setBatchFile(String batchFile) {
        this.batchFile = batchFile;
    }
    
    @CommandInput(
            consoleFormat = "-s",
            description = "Stop batch execution if one of the commands fails", 
            mandatory = false, 
            defaultValue = "false"
    )
    public void setStopExecution(boolean stop) {
        this.stop = stop; 
    }
        
}
