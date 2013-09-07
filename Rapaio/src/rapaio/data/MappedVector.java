/*
 * Copyright 2013 Aurelian Tutuianu
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

import java.util.Comparator;
import java.util.List;

/**
 * A vector which is build on the base of another vector and
 * the row order and which rows are selected is specified by a
 * mapping give at construction time.
 * <p/>
 * This vector does not hold actual values, it delegate the behavior
 * to the wrapped vector, thus the wrapping affects only the rows
 * selected anf the order of these rows.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedVector extends AbstractVector {

    private final Vector v;
    private final List<Integer> mapping;

    public MappedVector(Vector v, List<Integer> mapping) {
        this(v.getName(), v, mapping);
    }

    public MappedVector(String name, Vector v, List<Integer> mapping) {
        super(name);
        this.v = v;
        this.mapping = mapping;
    }

    @Override
    public int getRowCount() {
        return mapping.size();
    }

    @Override
    public boolean isNumeric() {
        return v.isNumeric();
    }

    @Override
    public boolean isNominal() {
        return v.isNominal();
    }

    @Override
    public int getRowId(int row) {
        return v.getRowId(mapping.get(row));
    }

    @Override
    public double getValue(int row) {
        return v.getValue(mapping.get(row));
    }

    @Override
    public void setValue(int row, double value) {
        v.setValue(mapping.get(row), value);
    }

    @Override
    public int getIndex(int row) {
        return v.getIndex(mapping.get(row));
    }

    @Override
    public void setIndex(int row, int value) {
        v.setIndex(mapping.get(row), value);
    }

    @Override
    public String getLabel(int row) {
        return v.getLabel(mapping.get(row));
    }

    @Override
    public void setLabel(int row, String value) {
        v.setLabel(mapping.get(row), value);
    }

    @Override
    public String[] dictionary() {
        return v.dictionary();
    }

    @Override
    public boolean isMissing(int row) {
        return v.isMissing(mapping.get(row));
    }

    @Override
    public void setMissing(int row) {
        v.setMissing(mapping.get(row));
    }

    @Override
    public Comparator<Integer> getComparator(boolean asc) {
        return v.getComparator(asc);
    }
}
