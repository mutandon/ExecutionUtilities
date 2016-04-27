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

import eu.unitn.disi.db.command.CommandInput;
import eu.unitn.disi.db.command.DynamicInput;
import eu.unitn.disi.db.command.PositionalInput;
import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.exceptions.WrongParameterException;
import eu.unitn.disi.db.mutilities.LoggableObject;
import eu.unitn.disi.db.mutilities.Pair;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
import static java.lang.System.currentTimeMillis;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * Abstract class to represents commands that can be executed
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public abstract class Command extends LoggableObject {
    
    /**
     * Parameters are the accepted input of the command along with their description
     */
    protected Map<String, String> descriptions;
    /**
     * A map containing the descriptions of the dynamic parameters (parameters
     * that can be load as a whole object)
     */
    protected Map<String, String> dynamicDescriptions;
    /**
     * A map containing the descriptions of the positional parameters
     */    
    protected Map<String, String> positionalDescriptions; 
    /*
     * Named command inputs used in init phase. 
     */
    private Map<String, Pair<Method, CommandInput>> namedParameters; 
    /*
     * Dynamic inputs used in init phase
     */
    private Map<String, Pair<Method, DynamicInput>> dynamicParameters;
    /*
     * Positional inputs used in init phase
     */    
    private Map<Integer, Pair<Method, PositionalInput>> positionalParameters; 
    /*
     * Execution time for the command.
     */
    private long executionTime; 
    /*
     * Determines if this command is a launcher
    */
    private boolean launcher;
    
    
    
    public Command() {
        loadReadableFields();
    }
        
    /**
     * This methods actually executes the command after having loaded all the paramenters
     * @throws ExecutionException If somethign wrong happens
     */
    protected abstract void execute() throws ExecutionException;
    /**
     * Personalize the description of the method in the help
     * @return A string to describe the command
     */
    protected abstract String commandDescription();
    
    
    /**
     * Returns an help that can be used in a user interface
     * @return The help message
     */
    public String help() {
        StringBuilder sb = new StringBuilder(commandDescription());        
        sb.append("\n");
        if (!positionalDescriptions.isEmpty()) {
            sb.append("\npositional arguments:\n");
            for (String cmd : positionalDescriptions.keySet()) {
                sb.append(cmd).append("\t").append(positionalDescriptions.get(cmd)).append("\n");
            }
        }
        if (!descriptions.isEmpty()) {
            sb.append("\nnamed arguments:\n");
            for (String cmd : descriptions.keySet()) {
                sb.append(cmd).append("\t").append(descriptions.get(cmd)).append("\n");
            }
        }
        if (!dynamicDescriptions.isEmpty()) {
            sb.append("\nwhere the following are dynamic parameters\n");
            for (String cmd : dynamicDescriptions.keySet()) {
                sb.append(cmd).append("\t").append(dynamicDescriptions.get(cmd)).append("\n");
            }
        }
        return sb.toString();
    }
    
    
    protected final void loadReadableFields() {
        Method[] methods = this.getClass().getMethods();
        CommandInput inputDescription;
        DynamicInput dynamicDescription;
        PositionalInput positionalDescription; 
        
        namedParameters = new HashMap<>();
        dynamicParameters = new HashMap<>();
        positionalParameters = new HashMap<>(); 
        descriptions = new LinkedHashMap<>();
        dynamicDescriptions = new LinkedHashMap<>();
        positionalDescriptions = new LinkedHashMap<>(); 
        String description;
                
        //Check method annotations and store into the respective variables
        for (Method method : methods) {
            inputDescription = method.getAnnotation(CommandInput.class);
            dynamicDescription = method.getAnnotation(DynamicInput.class);
            positionalDescription = method.getAnnotation(PositionalInput.class);
            
            if (inputDescription != null) {
                description = !inputDescription.mandatory()? "[optional] " : "";
                description += inputDescription.description();
                description += !"".equals(inputDescription.defaultValue())? " (default " + inputDescription.defaultValue() + ")" : "";
                descriptions.put(inputDescription.consoleFormat(), description);
                namedParameters.put(inputDescription.consoleFormat(), new Pair(method, inputDescription));
            }
            if (dynamicDescription != null) {
                description = dynamicDescription.description();
                dynamicDescriptions.put(dynamicDescription.consoleFormat(), description);
                dynamicParameters.put(dynamicDescription.consoleFormat(), new Pair(method, dynamicDescription));
            }
            if (positionalDescription != null) {
                description = positionalDescription.description();
                positionalDescriptions.put(positionalDescription.name(), description);
                positionalParameters.put(positionalDescription.position(), new Pair(method, positionalDescription));
            }
        }
    }
    
    
    protected void readParams(String[] params, Map<String, Object> dynamicObjects) 
            throws WrongParameterException {
        Method method = null;
        Object value;
        int i; 
        
        CommandInput inputDescription;
        
        try {
            //First process positional params, if present, then all the others
            for (i = 0; i < positionalDescriptions.size(); i++) {
                if (positionalParameters.containsKey(i + 1)) {
                    method = positionalParameters.get(i + 1).getFirst();
                    value = checkInputClass(method.getParameterTypes()[0], params[i]);
                    method.invoke(this, value);
                } else {
                    throw new WrongParameterException("Positional parameters are all mandatories, some of them are missing");
                }                
            }
            for (i = positionalDescriptions.size(); i < params.length; i++) {
                if (namedParameters.containsKey(params[i])) {
                    //Check length
                    //inputDescription = namedParameters.get(params[i]).getSecond();
                    method = namedParameters.get(params[i]).getFirst();
                    namedParameters.remove(params[i]);
                    //Check class
                    //Is a boolean 
                    if (method.getParameterTypes()[0].getName().equals("boolean") || method.getParameterTypes()[0].getName().equals("Boolean")) {
                        method.invoke(this, true);//Assign value
                    } else { 
                        value = checkInputClass(method.getParameterTypes()[0], params[i + 1]);
                        method.invoke(this, value);
                        i++;
                    }
                    //Remove from map (no duplicated entries)
                } else if (dynamicParameters.containsKey(params[i])) {
                    if (i + 1 >= params.length) {
                        throw new WrongParameterException("The number of parameters for %s must be 1", params[i]);
                    }
                    method = dynamicParameters.get(params[i]).getFirst();
                    value = dynamicObjects.get(params[i + 1]);
                    i++;
                    if (value == null) {
                        throw new WrongParameterException("The input parameter %s is not a valid variable", params[i + 1]);
                    }
                    method.invoke(this, value);
                } else {
                    throw new WrongParameterException("Input parameter %s is not a valid parameter", params[i]);
                }
            }
            //Check if some mandatory parameter is missing otherwise invoke the
            //set method with a default value
            Set<String> names = namedParameters.keySet();
            Pair<Method, CommandInput> input;
            for (String cmd : names) {
                input = namedParameters.get(cmd);
                method = input.getFirst();
                inputDescription = input.getSecond();
                if (inputDescription.mandatory()) {
                    throw new WrongParameterException("Parameter %s is mandatory", input.getSecond().consoleFormat());
                } else {
                    method.invoke(this, checkInputClass(method.getParameterTypes()[0], inputDescription.defaultValue()));
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            String methodName = "";
            if (method != null) {
                methodName = method.getName();
            }
            throw new WrongParameterException("Something happened while calling method '%s'", ex, methodName);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new WrongParameterException("Positional parameters are all mandatory.");
        } catch (WrongParameterException ex) {
            throw ex;
        }
    }
    
    protected Object checkInputClass(Class clazz, String... inputs) throws WrongParameterException {
        Object obj = null;
        String input = inputs[0];
        try {
            if (clazz.isArray()) {
                String[] array = input.split(",");
                Object instance = Array.newInstance(clazz.getComponentType(), array.length);
                if (instance instanceof Integer[] || instance instanceof int[]) {
                    for (int i = 0; i < array.length; i++) {
                         Array.set(instance, i, Integer.parseInt(array[i]));
                    }
                } else if (instance instanceof String[]) {
                    instance = array;
                } else if (instance instanceof Float[] || instance instanceof float[]) {
                    for (int i = 0; i < array.length; i++) {
                        Array.set(instance, i, Float.parseFloat(array[i]));
                    }
                } else if (instance instanceof Double[] || instance instanceof double[]) {
                    for (int i = 0; i < array.length; i++) {
                        Array.set(instance, i, Double.parseDouble(array[i]));
                    }
                } else if (instance instanceof Long[] || instance instanceof long[]) {
                    for (int i = 0; i < array.length; i++) {
                        Array.set(instance, i, Long.parseLong(array[i]));
                    }
                } else if (instance instanceof Short[] || instance instanceof short[]) {
                    for (int i = 0; i < array.length; i++) {
                        Array.set(instance, i, Short.parseShort(array[i]));
                    }
                } else {
                    throw new WrongParameterException("The input %s is not recognized as a valid class for %s", Arrays.toString(inputs), clazz.toString());
                }
                obj = instance;
            } else {
                Object instance = null;
                if (!clazz.isPrimitive()) {
                    instance = clazz.newInstance();
                }
                
                if (clazz.getName().equals("int") || instance instanceof Integer) {
                    obj = parseInt(input);
                } else if (instance instanceof String) {
                    obj = input;
                } else if (clazz.getName().equals("float") || instance instanceof Float) {
                    obj = parseFloat(input);
                } else if (clazz.getName().equals("double") || instance instanceof Double) {
                    obj = parseDouble(input);
                } else if (clazz.getName().equals("short") || instance instanceof Short) {
                    obj = parseShort(input);
                } else if (clazz.getName().equals("long") || instance instanceof Long) {
                    obj = parseLong(input);
                } else if (clazz.getName().equals("boolean") || instance instanceof Boolean) {
                    obj = parseBoolean(input);  
                } else if (clazz.getName().equals("char") || instance instanceof Character) {
                    obj = input.charAt(0);
                } else {
                    throw new WrongParameterException("The input %s is not recognized as a valid class for %s", Arrays.toString(inputs), clazz.toString());
                }
            }
            
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new WrongParameterException("The input %s is not recognized as a valid class for %s", Arrays.toString(inputs), clazz.toString());
        }
        return obj;
    }
    
    /**
     * Executes the command with the input parameters, it checks for the valid annotations. 
     * It cannot be changed by the subclasses
     * 
     * @param params The input parameters to be processed
     * @throws ExecutionException If something happens in the execution
     * @throws WrongParameterException If the input parameters are wrong
     */
    public final void exec(String[] params) throws ExecutionException, WrongParameterException 
    {
        exec(params, new HashMap<String, Object>());
    }

    
    public final void exec(String[] params, Map<String,Object> dynamicObjects) 
            throws ExecutionException, WrongParameterException 
    {
        readParams(params, dynamicObjects);
        executionTime = -currentTimeMillis();
        try {    
            execute();
        } catch (ExecutionException ex) {
            throw ex;
        }
        executionTime += currentTimeMillis();
        if (!this.getClass().isAnnotationPresent(ConsoleCommand.class)) {
            info("Command %s executed in %dms", this.getClass().getSimpleName(), executionTime);
        }
    }
    
    /**
     * Return last execution time (in milliseconds) for the command
     * @return last execution time in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
