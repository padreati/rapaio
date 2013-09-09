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

/**
 * A frame which is build based on another frame but with rows sorted by given criteria.
 *
 * @author Aurelian Tutuianu
 */
public class SortedFrame extends AbstractFrame {

    private final Frame df;
    private final ArrayList<Integer> mapping;

    public SortedFrame(Frame df, Comparator<Integer>... comparators) {
        this("", df, comparators);
    }

    public SortedFrame(String name, Frame df, Comparator<Integer>... comparators) {
        super(name);
        this.df = df;
        this.mapping = new ArrayList();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, new AggregateRowComparator(comparators));
    }

    @Override
    public int getRowCount() {
        return df.getRowCount();
    }

    @Override
    public int getColCount() {
        return df.getColCount();
    }

    @Override
    public int rowId(int row) {
        return mapping.get(row);
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
        return new SortedVector(df.getCol(col), mapping);
    }

    @Override
    public Vector getCol(String name) {
        return new SortedVector(df.getCol(name), mapping);
    }
}
