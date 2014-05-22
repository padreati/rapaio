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

package rapaio.data.mapping;

import rapaio.data.AbstractFrame;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Vector;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * A frame which is learn on the base of another frame with
 * the row order and row selection specified by a
 * getMapping give at construction time.
 * <p>
 * This frame does not hold actual values, it delegate the behavior
 * to the wrapped frame, thus the wrapping affects only the getRowCount
 * selected anf the order of these getRowCount.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrame extends AbstractFrame {

    private final Mapping mapping;
    private final Frame source;
    private final Numeric weights;
    private final String[] names;
    private final HashMap<String, Integer> colIndex;
    private final Vector[] vectors;


    public MappedFrame(Frame df, Mapping mapping) {
        this.mapping = mapping;
        if (df.isMappedFrame()) {
            this.source = df.sourceFrame();
        } else {
            this.source = df;
        }
        this.weights = new Numeric(mapping.rowStream().mapToDouble(source::getWeight).toArray());
        this.names = df.colNames();
        this.colIndex = new HashMap<>();
        this.vectors = new Vector[names.length];
        IntStream.range(0, names.length).forEach(i -> {
            colIndex.put(names[i], i);
            vectors[i] = new MappedVector(source.col(names[i]), mapping);
        });
    }

    public MappedFrame(Frame df, Mapping mapping, List<String> columns) {
        this.mapping = mapping;
        if (df.isMappedFrame()) {
            this.source = df.sourceFrame();
        } else {
            this.source = df;
        }
        this.weights = new Numeric(mapping.rowStream().mapToDouble(source::getWeight).toArray());
        this.names = new String[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            names[i] = columns.get(i);
        }
        this.colIndex = new HashMap<>();
        this.vectors = new Vector[names.length];
        IntStream.range(0, names.length).forEach(i -> {
            colIndex.put(names[i], i);
            vectors[i] = new MappedVector(source.col(names[i]), mapping);
        });
    }

    @Override
    public int rowCount() {
        return mapping.size();
    }

    @Override
    public int colCount() {
        return names.length;
    }

    @Override
    public int rowId(int row) {
        return mapping.get(row);
    }

    @Override
    public boolean isMappedFrame() {
        return true;
    }

    @Override
    public Frame sourceFrame() {
        return source;
    }


    @Override
    public Mapping mapping() {
        return mapping;
    }

    @Override
    public String[] colNames() {
        return names;
    }

    @Override
    public int colIndex(String name) {
        return colIndex.get(name);
    }

    @Override
    public Vector col(int col) {
        return vectors[col];
    }

    @Override
    public Vector col(String name) {
        return col(colIndex(name));
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
