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

import rapaio.data.util.AggregateRowComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A vector which is build based on another vector with row in
 * the order specified by given criteria.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SortedVector extends AbstractVector {

    private final Vector source;
    private final List<Integer> mapping;

    public SortedVector(Vector source, Comparator<Integer>... comparators) {
        this(source.getName(), source, comparators);
    }

    public SortedVector(String name, Vector source, Comparator<Integer>... comparators) {
        super(name);
        this.source = source;
        mapping = new ArrayList<>();
        for (int i = 0; i < source.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, new AggregateRowComparator(comparators));
    }

    public SortedVector(Vector source, List<Integer> mapping) {
        this(source.getName(), source, mapping);
    }

    public SortedVector(String name, Vector source, List<Integer> mapping) {
        super(name);
        this.source = source;
        this.mapping = mapping;
    }

    @Override
    public boolean isNumeric() {
        return source.isNumeric();
    }

    @Override
    public boolean isNominal() {
        return source.isNominal();
    }

    @Override
    public int getRowCount() {
        return source.getRowCount();
    }

    @Override
    public int getRowId(int row) {
        return source.getRowId(mapping.get(row));
    }

    @Override
    public int getIndex(int row) {
        return source.getIndex(mapping.get(row));
    }

    @Override
    public void setIndex(int row, int value) {
        source.setIndex(mapping.get(row), value);
    }

    @Override
    public double getValue(int row) {
        return source.getValue(mapping.get(row));
    }

    @Override
    public void setValue(int row, double value) {
        source.setValue(mapping.get(row), value);
    }

    @Override
    public String getLabel(int row) {
        return source.getLabel(mapping.get(row));
    }

    @Override
    public void setLabel(int row, String value) {
        source.setLabel(mapping.get(row), value);
    }

    @Override
    public String[] dictionary() {
        return source.dictionary();
    }

    @Override
    public boolean isMissing(int row) {
        return source.isMissing(mapping.get(row));
    }

    @Override
    public void setMissing(int row) {
        source.setMissing(mapping.get(row));
    }

    @Override
    public Comparator<Integer> getComparator(final boolean asc) {
        return new Comparator<Integer>() {
            private final Comparator<Integer> parentComp = source.getComparator(asc);

            @Override
            public int compare(Integer o1, Integer o2) {
                return parentComp.compare(mapping.get(o1), mapping.get(o2));
            }
        };
    }
}
