/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A mapping is a collection of row numbers used to build a mapped frame as a
 * wrapped selection of rows.
 * <p>
 * The mapping holds the rows which needs to be kept in the mapped frame.
 * <p>
 * If a mapped frame is built over another mapped frame, than the provided
 * mapping at creation time will be transformed into a mapped of the
 * solid frame which is referenced by the wrapped frame.
 * <p>
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
    static Mapping newCopyOf(int... mapping) {
        return new ListMapping(mapping);
    }

    static Mapping newRangeOf(int start, int end) {
        return new IntervalMapping(start, end);
    }

    /**
     * @return the size of mapping
     */
    int size();

    /**
     * Gets mapped index for given position
     *
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
     *
     * @param rows collection of row numbers to be added to the mapping
     */
    void addAll(Collection<Integer> rows);

    /**
     * Removes the element from the given position.
     *
     * @param pos position of the element which will be removed
     */
    void remove(int pos);

    /**
     * Removes all elements from the mapping
     *
     * @param positions collection with positions which will be removed
     */
    void removeAll(Collection<Integer> positions);

    /**
     * Removes all elements from mapping
     */
    void clear();


    /**
     * Builds a stream of indexes values
     *
     * @return a stream of indexed values
     */
    IntStream rowStream();
}

final class ListMapping implements Mapping {

    private static final long serialVersionUID = 5485844129188037454L;
    private final List<Integer> mapping;

    public ListMapping() {
        this.mapping = new ArrayList<>();
    }

    public ListMapping(int[] rows) {
        mapping = IntStream.of(rows).boxed().collect(Collectors.toList());
    }

    public ListMapping(List<Integer> mapping, boolean copy) {
        this.mapping = copy ? mapping.subList(0, mapping.size()) : mapping;
    }

    public int size() {
        return mapping.size();
    }

    public int get(int pos) {
        if (mapping.size() > pos)
            return mapping.get(pos);
        throw new IllegalArgumentException("Value at pos " + pos + " does not exists");
    }

    public void add(int pos) {
        mapping.add(pos);
    }

    public void addAll(Collection<Integer> pos) {
        mapping.addAll(pos);
    }

    @Override
    public void remove(int pos) {
        mapping.remove(pos);
    }

    @Override
    public void removeAll(Collection<Integer> positions) {
        positions.forEach(mapping::remove);
    }

    @Override
    public void clear() {
        mapping.clear();
    }

    public IntStream rowStream() {
        return mapping.stream().mapToInt(i -> i);
    }
}

final class IntervalMapping implements Mapping {

    final int start;
    final int end;
    boolean onList = false;
    ListMapping listMapping;

    IntervalMapping(int start, int end) {
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
    public void addAll(Collection<Integer> rows) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(IntStream.range(start, end).toArray());
        }
        listMapping.addAll(rows);
    }

    @Override
    public void remove(int pos) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(IntStream.range(start, end).toArray());
        }
        listMapping.remove(pos);
    }

    @Override
    public void removeAll(Collection<Integer> positions) {
        if (!onList) {
            onList = true;
            listMapping = new ListMapping(IntStream.range(start, end).toArray());
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
    public IntStream rowStream() {
        if (onList)
            return listMapping.rowStream();
        return IntStream.range(start, end);
    }
}