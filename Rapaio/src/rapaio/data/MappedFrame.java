/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.data;

import java.util.List;

/**
 * A frame which is build on the base of another frame with
 * the row order and row selection specified by a
 * mapping give at construction time.
 * <p/>
 * This frame does not hold actual values, it delegate the behavior
 * to the wrapped frame, thus the wrapping affects only the rows
 * selected anf the order of these rows.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrame extends AbstractFrame {

    private final Frame df;
    private final List<Integer> mapping;
    private final Vector[] vectors;

    public MappedFrame(Frame df, List<Integer> mapping) {
        this("", df, mapping);
    }

    public MappedFrame(String name, Frame df, List<Integer> mapping) {
        super(name);
        this.df = df;
        this.mapping = mapping;
        vectors = new Vector[df.getColCount()];
        for (int i = 0; i < vectors.length; i++) {
            vectors[i] = new MappedVector(df.getCol(i), mapping);
        }
    }

    @Override
    public int getRowCount() {
        return mapping.size();
    }

    @Override
    public int getColCount() {
        return vectors.length;
    }

    @Override
    public String[] getColNames() {
        return df.getColNames();
    }

    @Override
    public int getColIndex(String name) {
        return df.getColIndex(name);
    }

    @Override
    public Vector getCol(int col) {
        if (col < vectors.length) {
            return vectors[col];
        }
        return null;
    }

    @Override
    public Vector getCol(String name) {
        return df.getCol(name);
    }

    @Override
    public int rowId(int row) {
        return df.rowId(mapping.get(row));
    }

    @Override
    public double getValue(int row, int col) {
        return df.getValue(mapping.get(row), col);
    }

    @Override
    public void setValue(int row, int col, double value) {
        df.setValue(mapping.get(row), col, value);
    }

    @Override
    public int getIndex(int row, int col) {
        return df.getIndex(mapping.get(row), col);
    }

    @Override
    public void setIndex(int row, int col, int value) {
        df.setIndex(mapping.get(row), col, value);
    }

    @Override
    public String getLabel(int row, int col) {
        return df.getLabel(mapping.get(row), col);
    }

    @Override
    public void setLabel(int row, int col, String value) {
        df.setLabel(mapping.get(row), col, value);
    }
}
