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
 * A mapping is a collection of row numbers used to build a mapped frame as a
 * wrapped frame another one as a selection of rows.
 *
 * The mapping holds the rows which needs to be kept in the mapped frame.
 *
 * If a mapped frame is built over another mapped frame, than the provided
 * mapping at creation time will be transformed into a mapped of the
 * solid frame which is referenced by the wrapped frame.
 *
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Mapping extends Serializable {

    // static builders

    /**
     * @return an empty mapping
     */
    static Mapping newEmpty() {
        return new ListMapping();
    }

    /**
     * Builds a solid mapping which represents the identity
     * function map(row) = row.
     *
     * @param rowCount number of rows
     * @return solid mapping
     */
    static Mapping newSolidMap(int rowCount) {
        return new SolidMapping(rowCount);
    }

    /**
     * Builds a mapping having the mapped values specified as parameter,
     * the list of values being used as reference inside mapping.
     *
     * @param mapping list of mapped values
     * @return new mapping which wraps the given list of indexed values
     */
    static Mapping newWrapOf(List<Integer> mapping) {
        return new ListMapping(mapping, false);
    }

    /**
     * Builds a mapping having the mapped values given as a list of indexed values,
     * a copy of the list of values is used.
     *
     * @param mapping list of mapped values
     * @return new mapping which is build on a copy of the list of values
     */
    static Mapping newCopyOf(List<Integer> mapping) {
        return new ListMapping(mapping, false);
    }

    /**
     * Builds a mapping having the mapped values given as an array of indexed values,
     * a copy of the values is used.
     *
     * @param mapping array of mapped values
     * @return new mapping which is build on a copy of the array of values
     */
    static Mapping newCopyOf(int[] mapping) {
        return new ListMapping(mapping);
    }

    /**
     * @return the size of mapping
     */
    int size();

    /**
     * Gets mapped index for given position
     * @param pos given position
     * @return mapped value
     */
    int get(int pos);

    /**
     * Adds at the end of mapping a mapped index
     *
     * @param row mapped index
     */
    void add(int row);

    /**
     * Adds at the end of mapping the given indexes contained in collection
     * @param rows
     */
    void addAll(Collection<Integer> rows);

    /**
     * Sorts the mapping indexes according with the given comparators
     * @param comparators a list of comparators
     * @return a copied mapping which has indexes values according with the order given by comparators
     */
    Mapping sort(final Comparator<Integer>... comparators);

    /**
     * Builds a stream of indexes values
     *
     * @return a stream of indexed values
     */
    IntStream rowStream();
}

final class SolidMapping implements Mapping {

    private final int rowCountRO;

    public SolidMapping(int rowCount) {
        this.rowCountRO = rowCount;
    }

    public int size() {
        return rowCountRO;
    }

    public int get(int pos) {
        if (rowCountRO <= pos)
            throw new IllegalArgumentException("Illegal value " + pos + " for read only metadata");
        return pos;
    }

    public void add(int pos) {
        throw new NotImplementedException();
    }

    public void addAll(Collection<Integer> pos) {
        throw new NotImplementedException();
    }

    public Mapping sort(final Comparator<Integer>... comparators) {
        List<Integer> copy = Index.newSeq(rowCountRO).stream().map(VSpot::index).collect(Collectors.toList());
        Collections.sort(copy, RowComparators.aggregateComparator(comparators));
        return new ListMapping(copy, false);
    }

    public IntStream rowStream() {
        return IntStream.range(0, rowCountRO);
    }
}

final class ListMapping implements Mapping {

    private final List<Integer> mapping;

    public ListMapping() {
        this.mapping = new ArrayList<>();
    }

    public ListMapping(int[] rows) {
        mapping = new ArrayList<>(rows.length);
        for (int row : rows) {
            mapping.add(row);
        }
    }

    public ListMapping(List<Integer> mapping, boolean copy) {
        this.mapping = copy ? mapping.subList(0, mapping.size()) : mapping;
    }

    public int size() {
        return mapping.size();
    }

    public int get(int pos) {
        if (mapping != null) {
            if (mapping.size() > pos)
                return mapping.get(pos);
            throw new IllegalArgumentException("Value at pos " + pos + " does not exists");
        }
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
        ListMapping copy = new ListMapping(mapping, true);
        Collections.sort(copy.mapping, RowComparators.aggregateComparator(comparators));
        return copy;
    }

    public IntStream rowStream() {
        return mapping.stream().mapToInt(i -> i);
    }
}