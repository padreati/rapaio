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

package rapaio.data;

import rapaio.data.mapping.Mapping;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

/**
 * A frame which is not mapped, its values are contained in vectors.
 *
 * @author Aurelian Tutuianu
 */
public class SolidFrame extends AbstractFrame {

    private final int rows;
    private final Vector[] vectors;
    private final HashMap<String, Integer> colIndex;
    private final String[] names;
    private final Numeric weights;

    public SolidFrame(int rows, List<Vector> vectors, List<String> names) {
        this(rows, vectors, names.toArray(new String[names.size()]), null);
    }

    public SolidFrame(int rows, List<Vector> vectors, List<String> names, Numeric weights) {
        this(rows, vectors, names.toArray(new String[names.size()]), weights);
    }

    public SolidFrame(int rows, List<Vector> vectors, String[] names) {
        this(rows, vectors, names, null);
    }

    public SolidFrame(int rows, Vector[] vectors, String[] names, Numeric weights) {
        this(rows, Arrays.asList(vectors), names, weights);
    }

    public SolidFrame(int rows, Vector[] vectors, String[] names) {
        this(rows, Arrays.asList(vectors), names, null);
    }

    public SolidFrame(int rows, List<Vector> vectors, String[] names, Numeric weights) {
        for (Vector vector : vectors) {
            if (vector.isMappedVector())
                throw new IllegalArgumentException("Not allowed mapped vectors in solid frame");
        }
        this.rows = rows;
        this.vectors = new Vector[vectors.size()];
        if (weights != null)
            this.weights = weights;
        else {
            this.weights = new Numeric(rows);
            for (int i = 0; i < rows; i++) {
                this.weights.setValue(i, 1.0);
            }
        }
        this.colIndex = new HashMap<>();
        this.names = new String[vectors.size()];

        for (int i = 0; i < vectors.size(); i++) {
            this.vectors[i] = vectors.get(i);
            this.colIndex.put(names[i], i);
            this.names[i] = names[i];
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int colCount() {
        return vectors.length;
    }

    @Override
    public int rowId(int row) {
        return row;
    }

    @Override
    public boolean isMappedFrame() {
        return false;
    }

    @Override
    public Frame sourceFrame() {
        return this;
    }

    @Override
    public Mapping mapping() {
        return new Mapping(0, rowCount());
    }

    @Override
    public String[] colNames() {
        return names;
    }

    @Override
    public int colIndex(String name) {
        if (!colIndex.containsKey(name)) {
            throw new IllegalArgumentException("Invalid column name: " + name);
        }
        return colIndex.get(name);
    }

    @Override
    public Vector col(int col) {
        if (col >= 0 && col < vectors.length) {
            return vectors[col];
        }
        throw new IllegalArgumentException("Invalid column index: " + col);
    }

    @Override
    public Vector col(String name) {
        return col(colIndex(name));
    }

    @Override
    public boolean isMissing(int row, int col) {
        return col(col).isMissing(row);
    }

    @Override
    public boolean isMissing(int row, String colName) {
        return col(colName).isMissing(row);
    }

    @Override
    public Numeric getWeights() {
        return weights;
    }

    @Override
    public void setWeights(Numeric weights) {
        for (int i = 0; i < this.weights.rowCount(); i++) {
            this.weights.setValue(i, weights.getValue(i));
        }
    }

    @Override
    public double getWeight(int row) {
        return weights.getValue(row);
    }

    @Override
    public void setWeight(int row, double weight) {
        weights.setValue(row, weight);
    }
}
