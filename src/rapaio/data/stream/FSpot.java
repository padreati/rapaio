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

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class FSpot {

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

    public int rowId() {
        return df.rowId(row);
    }

    public boolean isMissing() {
        return df.isMissing(row);
    }

    public boolean isMissing(int colIndex) {
        return df.isMissing(row, colIndex);
    }

    public boolean isMissing(String colName) {
        return df.isMissing(row, colName);
    }

    public void setMissing(int colIndex) {
        df.setMissing(row, colIndex);
    }

    public void setMissing(String colName) {
        df.setMissing(row, colName);
    }

    public double getValue(int colIndex) {
        return df.getValue(row, colIndex);
    }

    public double getValue(String colName) {
        return df.getValue(row, colName);
    }

    public void setValue(int colIndex, double value) {
        df.setValue(row, colIndex, value);
    }

    public void setValue(String colName, double value) {
        df.setValue(row, colName, value);
    }

    public int getIndex(int colIndex) {
        return df.getIndex(row, colIndex);
    }

    public int getIndex(String colName) {
        return df.getIndex(row, colName);
    }

    public void setIndex(int colIndex, int value) {
        df.setIndex(row, colIndex, value);
    }

    public void setIndex(String colName, int value) {
        df.setIndex(row, colName, value);
    }

    public String getLabel(int colIndex) {
        return df.getLabel(row, colIndex);
    }

    public String getLabel(String colName) {
        return df.getLabel(row, colName);
    }

    public void setLabel(int colIndex, String value) {
        df.setLabel(row, colIndex, value);
    }

    public void setLabel(String colName, String value) {
        df.setLabel(row, colName, value);
    }

    public String[] getDictionary(int colIndex) {
        return df.col(colIndex).getDictionary();
    }

    public String[] getDictionary(String colName) {
        return df.col(colName).getDictionary();
    }
}
