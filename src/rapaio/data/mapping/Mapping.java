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

import rapaio.data.RowComparators;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Mapping implements Serializable {

    private final List<Integer> mapping;

    public Mapping() {
        this.mapping = new ArrayList<>();
    }

    public Mapping(int rowCount) {
        this.mapping = new ArrayList<>();
        IntStream.range(0, rowCount).forEach(mapping::add);
    }

    public Mapping(int[] rows) {
        mapping = new ArrayList<>(rows.length);
        for (int row : rows) {
            mapping.add(row);
        }
    }

    public Mapping(List<Integer> mapping) {
        this.mapping = mapping;
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

    public Mapping sort(final Comparator<Integer>... comparators) {
        Mapping copy = new Mapping(new ArrayList<>(mapping));
        Collections.sort(copy.mapping, RowComparators.aggregateComparator(comparators));
        return copy;
    }

    public IntStream rowStream() {
        return mapping.stream().mapToInt(i -> i);
    }
}
