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

import eu.unitn.disi.db.command.util.FileWriteOperation.Mode;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @return
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
     * @param colId
     * @return
     */
    public Double getAverage(int colId){
        return this.getAverage(this.getNumericFiedName(colId));
    }

    /**
     *
     * @param column
     * @return
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
     * @param colId
     * @return
     */
    public Double getMedian(int colId){
        return this.getMedian(this.getNumericFiedName(colId));
    }

    /**
     *
     * @param column
     * @return
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
     *
     * @return
     */
    public ArrayList<String> getFields(){
        ArrayList<String> fields = new ArrayList<>(this.stringColumns.size() + this.numericColumns.size() );
        fields.addAll(this.stringColumns);
        fields.addAll(this.numericColumns);
        return fields;
    }


    public String getStringFieldName(int i){
        return this.stringColumns.get(i);
    }

    public String getNumericFiedName(int i){
        return this.numericColumns.get(i);
    }

    /**
     *
     * @param i
     * @return
     */
    public ArrayList<String> getRow(int i){


        ArrayList<String> row = new ArrayList<>(this.stringColumns.size() + this.numericColumns.size());

        for(String field : this.stringColumns){
            row.add(this.stringValues.get(field).get(i));
        }

        for(String field : this.numericColumns){
            row.add(this.numericValues.get(field).get(i).toPlainString());
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

    public static void main(String[] args){

        ArrayList<Double> numsD = new ArrayList<>();
        ArrayList<Integer> numsI = new ArrayList<>();

        Statistics s = new Statistics();

        System.out.println("" +s.addStringField("Names"));

        System.out.println("" +s.addNumericField("Doubls"));
        System.out.println("" +s.addNumericField("Ints"));


        for (int i = 0; i < 13; i++) {

            Double dN = Math.random()*100+0.1*i;
            Integer iN = (int)Math.floor(dN.intValue());
            numsI.add(iN);
            numsD.add(dN);

            s.addStringValue("Names", "i: "+ iN + "  " + dN);
            s.addNumericValue("Doubls", dN);
            s.addNumericValue("Ints", iN);
        }



        for(List<String> row : s){
            System.out.println("" + row);
        }

        System.out.println("Doubles " + numsD);
        System.out.println("Doubles AVG " + s.getAverage(0)  + "   MDN " + s.getMedian("Doubls") );

        System.out.println("\n\nIntegers " + numsI);
        System.out.println("Integers AVG " + s.getAverage(1)  + "   MDN " + s.getMedian("Ints") );

        StatisticsCSVExporter se = new StatisticsCSVExporter(s, Mode.CREATE_NEW);
        try {
            se.write("/tmp/Export.csv");
        } catch (IOException ex) {
            Logger.getLogger(Statistics.class.getName()).log(Level.SEVERE, null, ex);
        }



    }

}
