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
package eu.unitn.disi.db.command.util.stats;

import eu.unitn.disi.db.command.util.FileWriteOperation;
import eu.unitn.disi.db.command.util.FileWriteOperation.Mode;
import eu.unitn.disi.db.mutilities.StringUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports a Statistic object with CSV format
 *
 *
 * @author Matteo Lissandrini <ml@disi.unitn.eu>
 */
public class StatisticsCSVExporter extends StatisticsExporter implements FileWriteOperation {

    private final Mode mode;
    private final Path fileLocation;

    public StatisticsCSVExporter(Statistics s, String fileLoc) {        
        this(s, fileLoc,  Mode.APPEND);
    }
    
    public StatisticsCSVExporter(Statistics s, String fileLoc, Mode mode) {
        super(s);
        this.mode = mode;
        this.fileLocation = Paths.get(fileLoc);
    }

    
    
    // TODO Export with or withou header
    @Override
    public String export() {

            StringBuilder export = new StringBuilder();

            ArrayList<String> fields = stat.getFields();

            String[] header = new String[fields.size()];
            int i = 0;
            for (String field : fields) {
                header[i++] = "\"" + field + "\"";
            }

            export.append(StringUtils.join(header, ",")).append("\n");

            for (List<String> row : stat) {
                export.append(StringUtils.join(row.toArray(new String[row.size()]), ",")).append(" \n");
            }



            return export.toString();

    }


    
    public void write() throws IOException {
        write(this.fileLocation, this.mode);
    }
    
    @Override
    public void write(Path p) throws IOException {
        write(p, this.mode);
    }

    @Override
    public void write(Path location, Mode m) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(location, StandardCharsets.UTF_8, m.getWriteOptions())) {
            writer.write(this.export());
        }
    }

    



}
