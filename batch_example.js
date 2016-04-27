/*
 * Copyright (C) 2015 Davide Mottin <mottin@disi.unitn.eu>
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
 * 
 * Example javascript to be executed in console command batch
 */

// The following two lines are mandatories, the variable "commands" must be present
// and filled with the string containing the commands to be run. 
var ArrayList = Java.type('java.util.ArrayList');
var commands = new ArrayList();
 
// Here put your code and fill the commands array
for (i = 0; i < 10; i++) {
    commands.add("exec (Dummy -w dummyparam)"); 
}
