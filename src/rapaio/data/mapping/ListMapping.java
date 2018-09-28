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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import rapaio.data.Mapping;

import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public final class ListMapping implements Mapping {

    private static final long serialVersionUID = 5485844129188037454L;
    private final IntList mapping;

    public ListMapping() {
        this.mapping = new IntArrayList();
    }

    public ListMapping(int[] rows) {
        mapping = IntArrayList.wrap(rows);
    }

    public ListMapping(IntList mapping, boolean copy) {
        this.mapping = copy ? new IntArrayList(mapping) : mapping;
    }

    public ListMapping(int start, int end) {
        mapping = new IntArrayList(end - start);
        for (int i = start; i < end; i++) {
            mapping.add(i);
        }
    }

    public ListMapping(IntList list, Int2IntFunction fun) {
        mapping = new IntArrayList(list.size());
        for (int i = 0; i < list.size(); i++) {
            mapping.add(fun.applyAsInt(list.getInt(i)));
        }
    }

    public int size() {
        return mapping.size();
    }

    public int get(int pos) {
        return mapping.getInt(pos);
    }

    public void add(int pos) {
        mapping.add(pos);
    }

    public void addAll(IntCollection pos) {
        mapping.addAll(pos);
    }

    @Override
    public void remove(int pos) {
        mapping.removeInt(pos);
    }

    @Override
    public void removeAll(IntCollection positions) {

        int[] toRemove = positions.toIntArray();
        IntArrays.quickSort(toRemove);

        int pos = 0;
        int last = 0;
        for (int i = 0; i < mapping.size(); i++) {
            if(i==toRemove[pos]) {
                pos++;
                continue;
            }
            mapping.set(last, mapping.getInt(i));
            last++;
        }
        mapping.size(last);
    }

    @Override
    public void clear() {
        mapping.clear();
    }

    @Override
    public IntListIterator iterator() {
        return mapping.iterator();
    }

    @Override
    public IntStream stream() {
        return mapping.stream().mapToInt(i->i);
    }

    @Override
    public IntList toList() {
        return mapping;
    }
}

