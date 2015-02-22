/* 
 * Example javascript to be executed in console command batch
 */

var ArrayList = Java.type('java.util.ArrayList');
var commands = new ArrayList();

for (i = 0; i < 10; i++) {
    commands.add("exec (Dummy -w dummyparam)"); 
}
