/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package rapaio.data.stream;

import rapaio.data.Frame;

import java.io.Serializable;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FSpot implements Serializable {

    private final Frame df;
    private final int row;

    public FSpot(Frame df, int row) {
        this.df = df;
        this.row = row;
    }

    public Frame getFrame() {
        return df;
    }

    public int row() {
        return row;
    }

    public boolean missing() {
        return df.missing(row);
    }

    public boolean missing(int colIndex) {
        return df.missing(row, colIndex);
    }

    public boolean missing(String colName) {
        return df.missing(row, colName);
    }

    public void setMissing(int colIndex) {
        df.setMissing(row, colIndex);
    }

    public void setMissing(String colName) {
        df.setMissing(row, colName);
    }

    public double value(int colIndex) {
        return df.value(row, colIndex);
    }

    public double value(String colName) {
        return df.value(row, colName);
    }

    public void setValue(int colIndex, double value) {
        df.setValue(row, colIndex, value);
    }

    public void setValue(String colName, double value) {
        df.setValue(row, colName, value);
    }

    public int index(int colIndex) {
        return df.index(row, colIndex);
    }

    public int index(String colName) {
        return df.index(row, colName);
    }

    public void setIndex(int colIndex, int value) {
        df.setIndex(row, colIndex, value);
    }

    public void setIndex(String colName, int value) {
        df.setIndex(row, colName, value);
    }

    public String label(int colIndex) {
        return df.label(row, colIndex);
    }

    public String label(String colName) {
        return df.label(row, colName);
    }

    public void setLabel(int colIndex, String value) {
        df.setLabel(row, colIndex, value);
    }

    public void setLabel(String colName, String value) {
        df.setLabel(row, colName, value);
    }

    public String[] dictionary(int colIndex) {
        return df.col(colIndex).dictionary();
    }

    public String[] dictionary(String colName) {
        return df.col(colName).dictionary();
    }

    public double weight() {
        return df.weight(row);
    }

    public void setWeight(double weight) {
        df.setWeight(row, weight);
    }
}
