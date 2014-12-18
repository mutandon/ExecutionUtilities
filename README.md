Execution Utilities 
==================

A small library to build commands in java using annotations on classes. 

This library allows a rapid development of commands in java, without bothering of reading arguments from main methods. 
The library contains also a console that can be used to dinamically load big objects and use them as though they are loaded into a web service. 

## Changes from version 1.0
1. Console commands are now extensible (with annotation @ConsoleCommand)
2. Batch experiements can be executed
3. History on command console
4. Added positional parameters
5. Better error managemente

## Usage

The basic units of _Execution Utilities_ are commands. To create a command that can be parsed through command line or using the command console (see below) just create a class that __extends Command__. Command defines two methods, namely execute and commandDescription. The first contains the command corpus itself, the second just a description to be visualized. 

Below you see and example command. 


```java
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
```

The use of the annotation above __set__ methods allows the parser to recognize parameters. The consoleFormat attribute allows the specification of the parameter name itself, while ther rest of the parameters define the numerability, the default value and the description (visible in the help).

A command may be invoked using. 

```
java -jar [jarname] [commandname] [parameters]
```

for example, supposing the jar containing the command definition is Test.jar, then you can call TestCommand simply issuing

```
java -jar Test.jar TestCommand -t Foo
```

that shows the output
```
Hello Foo, this is your first command!
```

All the commands should reside in the same package and the main for a command-enabled library (the library you are defining) should be like the following.

```java
    public static void main(String[] args) {
        if (System.getProperty("COMMANDS") == null) {
            System.setProperty("COMMANDS", "eu.unitn.disi.db.exemplar.commands");
        }
        CommandRunner.run(args);
    }
```
where ```eu.unitn.disi.db.exemplar.commands``` must be substituted with the package containing the commands. 


## Command console
_Execution Utilities_ contains also an input console that is built upon the idea: "Load all the jars dinamically and keep the big objects into main memory". 

Practically, once you build _Execution Utilities_ you can choose to run directly the console from the jar, you just need to type (assuming you have compiled Execution Utilities into ExecutionUtilities.jar) ```java -jar ExecutionUtilities.jar```. 

If everything goes right the command console should show a welcome message and a ">" to run commands. 

Type ```\?``` to visualize the possible directives accepted from the console. 

```
help [command]
	show the help for a specific command or this help if no command provided
jar JARFILE -lib LIBDIR
	load the jar file with the commands and the optiona libraries from LIBDIR 	directory
obj $VARIABLE LOADER [params]
	load or unload (if option -d $VARIABLE is present) an object into $variable using the specific loader
exec COMMAND [params]
	execute COMMAND with the specific parameters
batch BATCHFILE [-s] 
	execute a batch file with commands, put -s to stop
hist [-n ENTRIES] [-r]
```

* ```jar```defines the jar with the commands to be loaded, you can also optionally specify a lib directory.
* ```obj``` is specifically designed to load big objects into main memory and store into a variable (see below). The variable name can subsequantely be used in a command to pass objects to the command itself. 

## Loading objects into memory
Big objects, such as big graphs or indexes can be loaded once into memory and reused by different commands, while changing the jar with the commands. As long as the definition of the class is not in the jar loaded multiple times this allows you to change and test the code without reloading each time big objects. 

To load an object you should define a loader, which is a class of type ```LoaderCommand```. The below example represents a loader for BigMultigraphs (Grava library in github).

```java
public class BigMultigraphLoader extends LoaderCommand {
    private String graphPath; 
    
    @Override
    protected void execute() throws ExecutionException {
        try {
            loadedObject = new BigMultigraph(graphPath + "-sin.graph", graphPath + "-sout.graph", Utilities.countLines((graphPath + "-sin.graph")));
        } catch (ParseException ex) {
            throw new ExecutionException(ex);
        } catch (IOException ex) {
            throw new ExecutionException(ex);
        }
    }

    @CommandInput(parameters = ParametersNumber.TWO,
        consoleFormat = "-kb",
        defaultValue = "",
        description = "path to the knowledgbase sin and sout files, just up to the prefix, like InputData/freebase ",
        mandatory = true)
    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
    }    
    
    @Override
    protected String commandDescription() {
        return "Load a big multigraph into main memory";
    }

}
```

The loaded object can be stored in the console, while running and used in commands with the annotation ```@DynamicInput``` 

```java
    @DynamicInput(
            consoleFormat = "--graph",
            description = "multigraph used as a knowledge-base")
    public void setGraph(BigMultigraph graph) {
        this.graph = graph;
    }
```

In this way the input parameter must be first loaded in the console and then used passing a input name. 

**Notice.** This is only allowed in the command console. Using a dynamic input in normal console mode will throw and exception. 








