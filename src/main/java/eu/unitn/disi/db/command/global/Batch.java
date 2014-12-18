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
import eu.unitn.disi.db.command.util.Tokenizer;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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
        Tokenizer tok; 
        CharSequence[] tokenized; 
        String[] splittedLine; 
        int countTokens; 
        CharSequence value; 
        ExecutionService global = ExecutionService.getInstance();
        Object retval;
        
        p = FileSystems.getDefault().getPath(batchFile);
        try {
            lines = Files.readAllLines(p, Charset.defaultCharset());
            for (String line : lines) {
                if (line != null) {
                    line = line.trim(); 
                    if (!"".equals(line) && !line.startsWith(ExecutionService.BATCH_COMMENT)) {
                        tok = new Tokenizer(line);
                        tokenized = new String[256];
                        countTokens = 0;

                        tok.next();
                        while ((value = tok.value()) != null) {
                            tokenized[countTokens] = value;
                            tok.next();
                            countTokens++;
                        }
                        splittedLine = new String[countTokens];
                        System.arraycopy(tokenized, 0, splittedLine, 0, countTokens);
                        retval = global.runCommand(splittedLine, true);
                        if (stop && retval == ExecutionService.CommandError.ERROR) {
                            break; 
                        }
                    }
                }
            }
        } catch (IOException ex) {
            error("Cannot read batch file %s", batchFile);
        }
    }

    @Override
    protected String commandDescription() {
        return "Load and execute a batch of commans from file";
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
