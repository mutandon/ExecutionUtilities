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

import eu.unitn.disi.db.command.exceptions.ArgumentDeclarationException;

/**
 * A loader command is a special Command that is able to store one object into
 * memory. Is is particularly useful if used in conjunction with console. 
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public abstract class LoaderCommand extends Command {

    protected Object loadedObject;

    public LoaderCommand() throws ArgumentDeclarationException {
        super(); 
    }
    
    public Object getObject() {
        return loadedObject;
    }
}
