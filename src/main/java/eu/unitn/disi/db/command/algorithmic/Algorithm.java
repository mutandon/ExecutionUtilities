/*
 * Copyright (C) 2013 Davide Mottin <mottin@disi.unitn.eu>
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
package eu.unitn.disi.db.command.algorithmic;

import eu.unitn.disi.db.command.exceptions.AlgorithmExecutionException;
import eu.unitn.disi.db.mutilities.LoggableObject;
import eu.unitn.disi.db.mutilities.StopWatch;

/**
 * This class represents an algorithm which should provide input and output
 * parameters. The fact that two algorithms can be performed in series depends
 * on the kind of input.
 *
 * @author Davide Mottin <mottin@disi.unitn.eu>
 */
public abstract class Algorithm extends LoggableObject {

    protected final StopWatch timer = new StopWatch();

    /**
     * This methods should be called, and time s automatically measured.
     *
     * @throws AlgorithmExecutionException
     */
    public void compute() throws AlgorithmExecutionException {
        try {
            timer.reset();
            timer.start();
            this.algorithm();
            timer.stop();
        } catch (Exception e) {
            timer.stop();
            error("Algorithms %s interrupted after %s ms", e, this.getClass(), timer.getElapsedTimeMillis());
            throw e;
        }

    }

    /**
     * This method should be implemented and will be called by
     * Algorithm.compute();
     *
     * @throws AlgorithmExecutionException
     */
    protected abstract void algorithm() throws AlgorithmExecutionException;

    /**
     * Return the Elapsed time in milliseconds to run the algorithm
     *
     * @return The time elapsed
     * @see StopWatch
     */
    public long getComputationTime() {
        return timer.getElapsedTimeMillis();
    }

    public boolean checkInputs() throws IllegalArgumentException, IllegalAccessException, AlgorithmExecutionException {
        //        for (Field field : this.getClass().getDeclaredFields()) {
        //            if (field.isAnnotationPresent(AlgorithmInput.class)) {
        //                //TODO do check
        //                if(field.get(this) ==  null){
        //                    throw new AlgorithmExecutionException("The field %s for Algorithm %s cannot be NULL", field.getName(), this.getClass());
        //                }
        //            }
        //        }
        return true;
    }

}
