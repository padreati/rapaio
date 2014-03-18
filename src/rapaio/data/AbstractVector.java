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

import rapaio.data.collect.VInstance;
import rapaio.data.collect.VIterator;
import rapaio.data.mapping.MappedVector;
import rapaio.data.mapping.Mapping;

import java.util.*;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

/**
 * Base class for a vector which enforces to read-only name given at construction time.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractVector implements Vector {

    @Override
    public String toString() {
        return "Vector{ size='" + rowCount() + "\'}";
    }

    @Override
    public VIterator iterator() {
        return iterator(false);
    }

    @Override
    public VIterator iterator(boolean complete) {
        return new VectorIterator(complete, rowCount(), this);
    }

    @Override
    public VIterator cycleIterator(int size) {
        return new VectorIterator(true, size, this);
    }

    @Override
    public Stream<VInstance> stream() {
        List<VInstance> instances = new ArrayList<>();
        for (int i = 0; i < this.rowCount(); i++) {
            instances.add(new VInstanceImpl(i, this));
        }
        return instances.stream();
    }

    @Override
    public DoubleStream doubleStream() {
        throw new RuntimeException("Not implemented for this type of vector");
    }
}

class VectorIterator implements VIterator {

    private static final String DEFAULT_MAPPING_KEY = "$$DEFAULT$$";
    final boolean complete;
    final int size;
    final Vector vector;
    private final HashMap<String, Mapping> mappings = new HashMap<>();

    int pos = -1;
    int cyclePos = -1;

    VectorIterator(boolean complete, int size, Vector vector) {
        this.complete = complete;
        this.size = size;
        this.vector = vector;
    }

    @Override
    public boolean next() {
        while (pos < size - 1) {
            pos++;
            cyclePos++;
            if (cyclePos >= vector.rowCount()) {
                cyclePos = 0;
            }
            if (complete && vector.missing(cyclePos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        pos = -1;
        cyclePos = -1;
    }

    @Override
    public int getRowId() {
        return vector.rowId(cyclePos);
    }

    @Override
    public int getRow() {
        return cyclePos;
    }

    @Override
    public double getValue() {
        return vector.value(cyclePos);
    }

    @Override
    public void setValue(double value) {
        vector.setValue(cyclePos, value);
    }

    @Override
    public int getIndex() {
        return vector.index(cyclePos);
    }

    @Override
    public void setIndex(int value) {
        vector.setIndex(cyclePos, value);
    }

    @Override
    public String getLabel() {
        return vector.label(cyclePos);
    }

    @Override
    public void setLabel(String value) {
        vector.setLabel(cyclePos, value);
    }

    @Override
    public boolean isMissing() {
        return vector.missing(cyclePos);
    }

    @Override
    public void setMissing() {
        vector.setMissing(cyclePos);
    }

    @Override
    public void appendToMapping() {
        if (!mappings.containsKey(DEFAULT_MAPPING_KEY)) {
            mappings.put(DEFAULT_MAPPING_KEY, new Mapping());
        }
        mappings.get(DEFAULT_MAPPING_KEY).add(getRowId());
    }

    @Override
    public void appendToMapping(String key) {
        if (!mappings.containsKey(key)) {
            mappings.put(key, new Mapping());
        }
        mappings.get(key).add(getRowId());
    }

    @Override
    public int getMappingsCount() {
        return mappings.keySet().size();
    }

    @Override
    public Set<String> getMappingsKeys() {
        return mappings.keySet();
    }

    @Override
    public Mapping getMapping() {
        return mappings.get(DEFAULT_MAPPING_KEY);
    }

    @Override
    public Mapping getMapping(String key) {
        return mappings.get(key);
    }

    @Override
    public Vector getMappedVector() {
        return new MappedVector(vector.source(), getMapping());
    }

    @Override
    public Vector getMappedVector(String key) {
        return new MappedVector(vector.source(), getMapping(key));
    }

    @Override
    public Map<String, Vector> getMappedVectors() {
        Map<String, Vector> map = new HashMap<>();
        for (String key : getMappingsKeys()) {
            map.put(key, getMappedVector(key));
        }
        return map;
    }
}

final class VInstanceImpl implements VInstance {

    final int row;
    final Vector vector;

    VInstanceImpl(int row, Vector vector) {
        this.row = vector.rowId(row);
        this.vector = vector.source();
    }

    @Override
    public boolean missing() {
        return vector.missing(row);
    }

    @Override
    public void setMissing() {
        vector.setMissing(row);
    }

    @Override
    public double value() {
        return vector.value(row);
    }

    @Override
    public void setValue(final double value) {
        vector.setValue(row, value);
    }
}