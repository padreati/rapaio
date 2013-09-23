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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

    private final int rows;
    private final Vector[] vectors;
    private final HashMap<String, Integer> colIndex;
    private final String[] colNames;

    public MappedFrame(Frame df, Mapping mapping) {
        this(df.getName(), df, mapping);
    }

    public MappedFrame(String name, Frame df, Mapping mapping) {
        super(name);
        this.rows = mapping.size();
        this.colIndex = new HashMap<>();
        this.colNames = new String[df.getColCount()];
        for (int i = 0; i < df.getColCount(); i++) {
            colIndex.put(df.getColNames()[i], i);
            colNames[i] = df.getColNames()[i];
        }
        vectors = new Vector[df.getColCount()];

        Mapping cachedSolidMapping = mapping;
        HashMap<Mapping, Mapping> cachedMappedMappings = new HashMap<>();

        for (int i = 0; i < df.getColCount(); i++) {
            Vector old = df.getCol(i);
            if (!old.isMappedVector()) {
                vectors[i] = new MappedVector(old, cachedSolidMapping);
            } else {
                Mapping oldMapping = old.getMapping();
                if (!cachedMappedMappings.containsKey(oldMapping)) {
                    vectors[i] = new MappedVector(old, mapping);
                    cachedMappedMappings.put(oldMapping, vectors[i].getMapping());
                    continue;
                }
                vectors[i] = new MappedVector(old.getSourceVector(), cachedMappedMappings.get(oldMapping));
            }
        }
    }

    @Override
    public int getRowCount() {
        return rows;
    }

    @Override
    public int getColCount() {
        return vectors.length;
    }

    @Override
    public int getRowId(int row, int col) {
        return getCol(col).getRowId(row);
    }

    @Override
    public String[] getColNames() {
        return colNames;
    }

    @Override
    public int getColIndex(String name) {
        return colIndex.get(name);
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
        return getCol(getColIndex(name));
    }

//    @Override
//    public double getValue(int row, int col) {
//        return getCol(col).getValue(row);
//    }
//
//    @Override
//    public void setValue(int row, int col, double value) {
//        getCol(col).setValue(row, value);
//    }
//
//    @Override
//    public int getIndex(int row, int col) {
//        return getCol(col).getIndex(row);
//    }
//
//    @Override
//    public void setIndex(int row, int col, int value) {
//        getCol(col).setIndex(row, value);
//    }
//
//    @Override
//    public String getLabel(int row, int col) {
//        return getCol(col).getLabel(row);
//    }
//
//    @Override
//    public void setLabel(int row, int col, String value) {
//        getCol(col).setLabel(row, value);
//    }
}
