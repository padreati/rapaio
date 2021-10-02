/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.mapping;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.IntStream;

import rapaio.core.RandomSource;
import rapaio.data.Mapping;
import rapaio.util.IntIterator;
import rapaio.util.collection.IntArrayList;
import rapaio.util.collection.IntArrays;
import rapaio.util.function.Int2IntFunction;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public final class ArrayMapping implements Mapping {

    @Serial
    private static final long serialVersionUID = 5485844129188037454L;
    private final IntArrayList data;

    public ArrayMapping() {
        this.data = new IntArrayList(0);
    }

    public ArrayMapping(int[] array, int start, int end) {
        this.data = new IntArrayList(array, start, end - start);
    }

    public ArrayMapping(int start, int end) {
        this.data = new IntArrayList(IntArrays.newSeq(start, end));
    }

    public ArrayMapping(int[] array, int start, int end, Int2IntFunction fun) {
        this.data = new IntArrayList(IntArrays.newFrom(array, start, end, fun));
    }

    public int size() {
        return data.size();
    }

    public int get(int pos) {
        return data.getInt(pos);
    }

    @Override
    public void add(int value) {
        data.ensureCapacity(data.size() + 1);
        this.data.add(value);
    }

    @Override
    public void addAll(IntIterator it) {
        while (it.hasNext()) {
            add(it.nextInt());
        }
    }

    @Override
    public void remove(int pos) {
        data.removeInt(pos);
    }

    @Override
    public void removeAll(IntIterator it) {
        int offset = 0;
        while (it.hasNext()) {
            remove(it.nextInt() - (offset++));
        }
    }

    @Override
    public void clear() {
        data.clear();
    }

    @Override
    public IntIterator iterator() {
        return IntArrays.iterator(data.elements(), 0, data.size());
    }

    @Override
    public int[] elements() {
        return data.elements();
    }

    @Override
    public void shuffle() {
        IntArrays.shuffle(data.elements(), 0, data.size(), RandomSource.getRandom());
    }

    @Override
    public IntStream stream() {
        return Arrays.stream(data.elements(), 0, data.size());
    }
}

