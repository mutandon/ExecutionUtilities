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
package eu.unitn.disi.db.command;

import eu.unitn.disi.db.command.exceptions.ExecutionException;
import eu.unitn.disi.db.command.exceptions.WrongParameterException;
import eu.unitn.disi.db.command.util.LoggableObject;
import eu.unitn.disi.db.command.util.Pair;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    /*
     * Used in init phase. 
     */
    private Map<String, Pair<Method, CommandInput>> commandInputs; 
    /*
     * Dynamic inputs used in init phase
     */
    private Map<String, Pair<Method, DynamicInput>> dynamicInputs;
    /*
     * Execution time for the command.
     */
    private long executionTime; 
    /*
     * Variable used to stop the command
     */
    private boolean stopped; 
//    /*
//     * This map contains referencse to objects that are preloaded and used as they are
//     */
//    private Map<String, Object> dynamicObjects;
    
    private class StopThread implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            try {
                while (!stopped) {
                    Thread.currentThread().sleep(100);
                }
                throw new Exception();
            } catch (InterruptedException ex) {
                return true;
            }
        }   
    }
    
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
        for (String cmd : descriptions.keySet()) {
            sb.append(cmd).append("\t").append(descriptions.get(cmd)).append("\n");
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
        commandInputs = new HashMap<String, Pair<Method, CommandInput>>();
        dynamicInputs = new HashMap<String, Pair<Method, DynamicInput>>();
        descriptions = new LinkedHashMap<String, String>();
        dynamicDescriptions = new LinkedHashMap<String, String>();
        
        String description;
        
        for (int i = 0; i < methods.length; i++) {
            inputDescription = methods[i].getAnnotation(CommandInput.class);
            dynamicDescription = methods[i].getAnnotation(DynamicInput.class);
            if (inputDescription != null) {
                description = !inputDescription.mandatory()? "[optional] " : "";
                description += inputDescription.description();
                description += !"".equals(inputDescription.defaultValue())? " (default " + inputDescription.defaultValue() + ")" : "";
                descriptions.put(inputDescription.consoleFormat(), description);
                commandInputs.put(inputDescription.consoleFormat(), new Pair(methods[i], inputDescription));
            }
            if (dynamicDescription != null) {
                description = dynamicDescription.description();
                dynamicDescriptions.put(dynamicDescription.consoleFormat(), description);
                dynamicInputs.put(dynamicDescription.consoleFormat(), new Pair(methods[i], dynamicDescription));
            }
        }
    }
    
    protected void readParams(String[] params, Map<String, Object> dynamicObjects) throws WrongParameterException {
        Method method;
        Object value;
        int numPars;
        CommandInput inputDescription;
        
        try {
            for (int i = 0; i < params.length; i++) {
                if (commandInputs.containsKey(params[i])) {
                    //Check length
                    inputDescription = commandInputs.get(params[i]).getSecond();
                    method = commandInputs.get(params[i]).getFirst();
                    numPars = inputDescription.parameters().getNum();
                    if (i + numPars > params.length) {
                        throw new WrongParameterException("The number of parameters for %s must be %d", params[i], inputDescription.parameters().getNum());
                    }
                    //Check class
                    if (numPars == 2) {
                        //Assign value
                        //Dynamic parameter
                        value = checkInputClass(method.getParameterTypes()[0], Arrays.copyOfRange(params, i + 1, i + numPars));
                        method.invoke(this, value);
                    } else if (numPars == 1) { //Is a boolean 
                        method.invoke(this, true);
                    } else {
                        throw new WrongParameterException("The number of input parameters cannot be greater than 2");
                    }
                    //Remove from map (no duplicated entries)
                    commandInputs.remove(params[i]);
                } else if (dynamicInputs.containsKey(params[i])) {
                    if (i + 1 >= params.length) {
                        throw new WrongParameterException("The number of parameters for %s must be 1", params[i]);
                    }
                    method = dynamicInputs.get(params[i]).getFirst();
                    value = dynamicObjects.get(params[i + 1]);
                    if (value == null) {
                        throw new WrongParameterException("The input parameter %s is not a valid variable", params[i + 1]);
                    }
//                    System.out.println("value.type=" + value.getClass().getCanonicalName());
//                    System.out.println("method.type=" + method.getParameterTypes()[0].getCanonicalName());
//                    System.out.println("convertible=" + value.getClass().isAssignableFrom(method.getParameterTypes()[0]));
                    
                    method.invoke(this, value);
                }
            }
            //Set all the unexpressed files.
            Set<String> names = commandInputs.keySet();
            Pair<Method, CommandInput> input;
            for (String cmd : names) {
                input = commandInputs.get(cmd);
                method = input.getFirst();
                inputDescription = input.getSecond();
                if (inputDescription.mandatory()) {
                    throw new WrongParameterException("Parameter %s is mandatory", input.getSecond().consoleFormat());
                } else {
                    method.invoke(this, checkInputClass(method.getParameterTypes()[0], inputDescription.defaultValue()));
                }
            }
        } catch (Exception ex) {
            throw new WrongParameterException(ex);
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
                    obj = Integer.parseInt(input);
                } else if (instance instanceof String) {
                    obj = input;
                } else if (clazz.getName().equals("float") || instance instanceof Float) {
                    obj = Float.parseFloat(input);
                } else if (clazz.getName().equals("double") || instance instanceof Double) {
                    obj = Double.parseDouble(input);
                } else if (clazz.getName().equals("short") || instance instanceof Short) {
                    obj = Short.parseShort(input);
                } else if (clazz.getName().equals("long") || instance instanceof Long) {
                    obj = Long.parseLong(input);
                } else if (clazz.getName().equals("boolean") || instance instanceof Boolean) {
                    obj = Boolean.parseBoolean(input);  
                } else if (clazz.getName().equals("char") || instance instanceof Character) {
                    obj = input.charAt(0);
                } else {
                    throw new WrongParameterException("The input %s is not recognized as a valid class for %s", Arrays.toString(inputs), clazz.toString());
                }
            }
            
        } catch (Exception ex) {
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
    public final void exec(String[] params) throws ExecutionException, WrongParameterException {
        exec(params, new HashMap<String, Object>());
    }

    
    public final void exec(String[] params, Map<String,Object> dynamicObjects) throws ExecutionException, WrongParameterException {
        readParams(params, dynamicObjects);
        executionTime = -System.currentTimeMillis();
        //ExecutorService pool = Executors.newCachedThreadPool();
        //pool.submit(new StopThread()).get;
        try {
            info("Executing command %s %s", this.getClass().getSimpleName(), Arrays.toString(params));
            execute();
        } catch (ExecutionException ex) {
            //pool.shutdownNow();
            throw ex;
        }
        executionTime += System.currentTimeMillis();
        info("Command %s executed in %dms", this.getClass().getSimpleName(), executionTime);
    }

    public void stop() {
        stopped = true;
    }
    
    /**
     * Return last execution time (in milliseconds) for the command
     * @return last execution time in milliseconds
     */
    public long getExecutionTime() {
        return executionTime;
    }
}
