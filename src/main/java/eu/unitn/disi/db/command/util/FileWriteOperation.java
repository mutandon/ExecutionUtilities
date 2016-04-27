/*
 * Copyright (C) 2015 Matteo Lissandrini <ml@disi.unitn.eu>
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
package eu.unitn.disi.db.command.util;

import java.io.IOException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Interface for any operation that writes to a File
 * Handles correct Open Options for writing to files
 *
 * Supported configurations are in {@link #Mode}
 *
 *
 * @author Matteo Lissandrini <ml@disi.unitn.eu>
 */
public interface FileWriteOperation {

    public enum Mode {

        /**
         * Create a new file, failing if the file already exists. The check for
         * the existence of the file and the creation of the file if it does not
         * exist is atomic with respect to other file system operations.
         */
        CREATE_NEW,
        /**
         * If the file already exists and it is opened, then its length is
         * truncated to 0 before writing to it.
         */
        OVERWRITE,
        /**
         * If the file is opened then bytes will be written to the end of the
         * file rather than the beginning.
         *
         */
        APPEND;

        /**
         * the options configuration needed to pass to a BufferedWriter in order
         * to obtain the desired write behavior
         *
         * @return the array of Open Options to be provided to the Writer
         */
        public OpenOption[] getWriteOptions() {
            final OpenOption[] opts = new OpenOption[3];

            switch (this) {
                case OVERWRITE:
                    opts[0] = StandardOpenOption.CREATE;
                    opts[1] = StandardOpenOption.TRUNCATE_EXISTING;
                    break;

                case CREATE_NEW:
                    opts[0] = StandardOpenOption.CREATE_NEW;
                    opts[1] = StandardOpenOption.APPEND;
                    break;

                case APPEND:
                default:
                    opts[0] = StandardOpenOption.CREATE;
                    opts[1] = StandardOpenOption.APPEND;
                    break;
            }
            opts[2] = StandardOpenOption.WRITE;

            return opts;

        }

    }

    /**
     * Writes the string to file with the specified Behavior
     *
     * @param p the path to the file to write onto
     * @param m the OpenOption Mode
     * @throws IOException
     */
    public void write(Path p, Mode m) throws IOException;

    /**
     * Writes the string to file with a non specified Default mode
     *
     * @param p the path to the file to write onto
     * @throws IOException
     */
    public void write(Path p) throws IOException;

}
