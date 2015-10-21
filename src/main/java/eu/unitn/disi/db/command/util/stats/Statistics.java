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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Matteo Lissandrini <ml@disi.unitn.eu>
 */
public class Statistics implements Iterable<List<String>>{

    private final ArrayList<String> numericColumns;
    private final ArrayList<String> stringColumns;
    private final HashMap<String, LinkedList<BigDecimal>> numericValues;
    private final HashMap<String, LinkedList<String>> stringValues;

    private final MathContext mc;


    public Statistics() {
        this.numericColumns = new ArrayList<>();
        this.stringColumns = new ArrayList<>();

        this.numericValues = new HashMap<>();
        this.stringValues = new HashMap<>();

        this.mc = MathContext.DECIMAL128;
    }

    public Statistics(int scale, RoundingMode rounding) {
        this.numericColumns = new ArrayList<>();
        this.stringColumns = new ArrayList<>();
        this.numericValues = new HashMap<>();
        this.stringValues = new HashMap<>();

        this.mc = new MathContext(scale, rounding);
    }





    /**
     *
     * @param name
     * @return true if the filed was added, false if the field exists already
     */
    public boolean addStringField(String name){
        if(stringValues.containsKey(name)){
            return false;
        }

        stringColumns.add(name);
        stringValues.put(name, new LinkedList<String>());

        return true;

    }


    /**
     *
     * @param name
     * @return true if the filed was added, false if the field exists already
     */
    public boolean addNumericField(String name){
        if(numericValues.containsKey(name)){
            return false;
        }

        numericColumns.add(name);
        numericValues.put(name, new LinkedList<BigDecimal>());

        return true;

    }



    /**
     *
     * @param column
     * @param value
     * @return the number of values in the column, after adding the passed value
     */
    public int addStringValue(String column, String value){
        if(!stringValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of String type");
        }

        stringValues.get(column).add(value);

        return stringValues.get(column).size();
    }




    /**
     *
     * @param column
     * @param value
     * @return the number of values in the column, after adding the passed value
     */
    public int addNumericValue(String column, Double value){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        numericValues.get(column).add(new BigDecimal(value, this.mc));

        return numericValues.get(column).size();
    }

    /**
     *
     * @param column
     * @param value
     * @return the number of values in the column, after adding the passed value
     */
    public int addNumericValue(String column, Integer value){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        numericValues.get(column).add(new BigDecimal(value, this.mc));

        return numericValues.get(column).size();
    }

    /**
     *
     * @param column
     * @param value
     * @return the number of values in the column, after adding the passed value
     */
    public int addNumericValue(String column, Long value){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        numericValues.get(column).add(new BigDecimal(value, this.mc));

        return numericValues.get(column).size();
    }


    /**
     *
     * @param column
     * @param value
     * @return the number of values in the column, after adding the passed value
     */
    public int addNumericValue(String column, BigDecimal value){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        numericValues.get(column).add(value);

        return numericValues.get(column).size();
    }


    /**
     *
     * @param i
     * @return the average of the values in the i-th column
     */
    public Double getAverage(int i){
        return this.getAverage(this.getNumericFiedName(i));
    }

    /**
     *
     * @param column
     * @return the average of the values in that column
     */
    public Double getAverage(String column){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        LinkedList<BigDecimal> values = numericValues.get(column);
        int size = values.size();

        BigDecimal sum = new BigDecimal("0", mc);

        for (BigDecimal value : values) {
            sum = sum.add(value, mc);
        }

        return sum.divide(new BigDecimal(size), mc).doubleValue();
    }



    /**
     *
     * @param i
     * @return the median of the valued in the i-th column
     */
    public Double getMedian(int i){
        return this.getMedian(this.getNumericFiedName(i));
    }

    /**
     *
     * @param column
     * @return the median of the values in that column
     */
    public Double getMedian(String column){
        if(!numericValues.containsKey(column)){
            throw new IllegalArgumentException("Columns " + column + " does not exists or is not of Numeric type");
        }

        LinkedList<BigDecimal> values = numericValues.get(column);
        int size = values.size();
        Double median;
        Collections.sort(values);

        if (size % 2 == 0){
            median = values.get(size/2).add( values.get(size/2 - 1)).divide(new BigDecimal("2"), mc).doubleValue();
        }
        else {
            median = values.get(size/2).doubleValue();
        }


        return median;
    }

    /**
     * Get all the names of all the columns, textual fields first, then numeric fields
     *
     * @return the list of field names
     */
    public ArrayList<String> getFields(){
        ArrayList<String> fields = new ArrayList<>(this.stringColumns.size() + this.numericColumns.size() );
        fields.addAll(this.stringColumns);
        fields.addAll(this.numericColumns);
        return fields;
    }

    /**
     * Get the name of the i-th field among the textual columns
     * @param i
     * @return the name of the field
     */
    public String getStringFieldName(int i){
        return this.stringColumns.get(i);
    }

    /**
     * Get the name of the i-th field among the numeric columns
     * @param i
     * @return the name of the field
     */
    public String getNumericFiedName(int i){
        return this.numericColumns.get(i);
    }

    /**
     * Get the row at i-th position in the the table of statistics
     * @param i
     * @return the Row as List of Strings, each String is the value in une cell
     */
    public ArrayList<String> getRow(int i){


        ArrayList<String> row = new ArrayList<>(this.stringColumns.size() + this.numericColumns.size());

        for(String field : this.stringColumns){
            try {
            row.add(this.stringValues.get(field).get(i));
            } catch (IndexOutOfBoundsException ie) {
                throw  new IndexOutOfBoundsException( "Impossible to get row " + i + " field " + field +" is incomplete" );
            }
        }

        for(String field : this.numericColumns){
            try {
                row.add(this.numericValues.get(field).get(i).toPlainString());
            } catch (IndexOutOfBoundsException ie) {
                throw  new IndexOutOfBoundsException( "Impossible to get row " + i + " field " + field +" is incomplete" );
            }

        }

        return row;

    }

    @Override
    public Iterator<List<String>> iterator() {
        return new RowIterator();
    }


    private class RowIterator implements Iterator<List<String>> {

        private int rowIdx = 0;

        @Override
        public boolean hasNext() {
            if(!stringColumns.isEmpty()) {
                return rowIdx < stringValues.get(stringColumns.get(0)).size();
            }
            if(!numericColumns.isEmpty()) {
                return rowIdx < numericValues.get(numericColumns.get(0)).size();
            }
            return false;
        }

        @Override
        public List<String> next() {
            return getRow(rowIdx++);
        }

    }
}
