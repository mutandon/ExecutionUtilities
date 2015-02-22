/* 
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
