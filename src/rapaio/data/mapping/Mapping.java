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

import rapaio.data.Index;
import rapaio.data.RowComparators;
import rapaio.data.stream.VSpot;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class Mapping implements Serializable {

    private final List<Integer> mapping;
    private final int rowCountRO;

    public static Mapping newEmpty() {
        return new Mapping();
    }

    public static Mapping newSeqRO(int rowCount) {
        return new Mapping(rowCount, true);
    }

    public static Mapping newSeq(int rowCount) {
        return new Mapping(rowCount, false);
    }

    public static Mapping newWrapOf(List<Integer> mapping) {
        return new Mapping(mapping);
    }

    public static Mapping newCopyOf(int[] rows) {
        return new Mapping(rows);
    }

    private Mapping() {
        this.mapping = new ArrayList<>();
        this.rowCountRO = -1;
    }

    private Mapping(int rowCount, boolean readOnly) {
        if (readOnly) {
            this.mapping = null;
            this.rowCountRO = rowCount;
        } else {
            this.mapping = IntStream.range(0, rowCount).mapToObj(row -> row).collect(Collectors.toList());
            this.rowCountRO = -1;
        }
    }

    private Mapping(int[] rows) {
        mapping = new ArrayList<>(rows.length);
        rowCountRO = -1;
        for (int row : rows) {
            mapping.add(row);
        }
    }

    private Mapping(List<Integer> mapping) {
        this.mapping = mapping;
        this.rowCountRO = -1;
    }

    public int size() {
        return mapping != null ? mapping.size() : rowCountRO;
    }

    public int get(int pos) {
        if (mapping != null) {
            if (mapping.size() > pos)
                return mapping.get(pos);
            throw new IllegalArgumentException("Value at pos " + pos + " does not exists");
        }
        if (rowCountRO <= pos)
            throw new IllegalArgumentException("Illegal value " + pos + " for read only metadata");
        return pos;
    }

    public void add(int pos) {
        if (mapping != null)
            mapping.add(pos);
        else
            throw new NotImplementedException();
    }

    public void addAll(Collection<Integer> pos) {
        if (mapping != null)
            mapping.addAll(pos);
        else throw new NotImplementedException();
    }

    public Mapping sort(final Comparator<Integer>... comparators) {
        Mapping copy = mapping != null
                ? new Mapping(new ArrayList<>(mapping))
                : new Mapping(Index.newSeq(rowCountRO).stream().map(VSpot::index).collect(Collectors.toList()));
        Collections.sort(copy.mapping, RowComparators.aggregateComparator(comparators));
        return copy;
    }

    public IntStream rowStream() {
        return mapping != null ? mapping.stream().mapToInt(i -> i) : IntStream.range(0, rowCountRO);
    }
}
