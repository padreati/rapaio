/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
 *
 */

package rapaio.data.mapping;

import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import rapaio.data.Mapping;

import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public final class IntervalMapping implements Mapping {

    private static final long serialVersionUID = -7421133121383028265L;

    private final int start;
    private final int end;
    private boolean onList = false;
    private ListMapping listMapping;

    public IntervalMapping(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int size() {
        if (onList)
            return listMapping.size();
        return end - start;
    }

    @Override
    public int get(int pos) {
        if (onList)
            return listMapping.get(pos);
        return pos + start;
    }

    @Override
    public void add(int row) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(IntStream.range(start, end).toArray());
        }
        listMapping.add(row);
    }

    @Override
    public void addAll(IntCollection rows) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(start, end);
        }
        listMapping.addAll(rows);
    }

    @Override
    public void remove(int pos) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(start, end);
        }
        listMapping.remove(pos);
    }

    @Override
    public void removeAll(IntCollection positions) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(start, end);
        }
        listMapping.removeAll(positions);
    }

    @Override
    public void clear() {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(IntStream.range(start, end).toArray());
        }
        listMapping.clear();
    }

    @Override
    public IntListIterator iterator() {
        return onList ? listMapping.iterator() : new IntListIterator() {

            int s = start;

            @Override
            public boolean hasPrevious() {
                return s > start;
            }

            @Override
            public boolean hasNext() {
                return s < end;
            }

            @Override
            public int nextIndex() {
                return s - start;
            }

            @Override
            public int previousIndex() {
                return s - start - 1;
            }

            @Override
            public int previousInt() {
                s--;
                return s;
            }

            @Override
            public int nextInt() {
                s++;
                return s-1;
            }
        };
    }

    @Override
    public IntStream stream() {
        return onList ? listMapping.stream() : IntStream.range(start, end);
    }

    @Override
    public IntList toList() {
        if (onList) {
            return listMapping.toList();
        }
        return new IntervalIntList(start, end);
    }
}

class IntervalIntList extends AbstractIntList {

    private final int start;
    private final int end;

    IntervalIntList(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public int getInt(int i) {
        return i + start;
    }

    @Override
    public int size() {
        return end - start;
    }
}