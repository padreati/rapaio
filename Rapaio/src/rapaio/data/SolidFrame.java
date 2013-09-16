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

/*
 * 
 */
package rapaio.data;

import java.util.HashMap;

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

    private final int rows;
    private final Vector[] vectors;
    private final HashMap<String, Integer> mapping;
    private final String[] names;

    public SolidFrame(String name, int rows, Vector[] vectors) {
        super(name);
        this.rows = rows;
        this.vectors = new Vector[vectors.length];
        this.mapping = new HashMap<>();
        this.names = new String[vectors.length];

        for (int i = 0; i < vectors.length; i++) {
            this.vectors[i] = vectors[i];
            this.mapping.put(vectors[i].getName(), i);
            names[i] = vectors[i].getName();
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
    public int rowId(int row) {
        return row;
    }

    @Override
    public String[] getColNames() {
        return names;
    }

    @Override
    public int getColIndex(String name) {
        if (!mapping.containsKey(name)) {
            throw new IllegalArgumentException("Column name is invalid");
        }
        return mapping.get(name);
    }

    @Override
    public Vector getCol(int col) {
        if (col >= 0 && col < vectors.length) {
            return vectors[col];
        }
        throw new IllegalArgumentException("Invalid column index");
    }

    @Override
    public Vector getCol(String name) {
        return getCol(getColIndex(name));
    }
}
