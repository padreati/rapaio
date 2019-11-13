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

import rapaio.core.RandomSource;
import rapaio.data.Mapping;
import rapaio.util.collection.IntArrays;
import rapaio.util.collection.IntIterator;
import rapaio.util.function.IntIntFunction;

import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public final class ArrayMapping implements Mapping {

    private static final long serialVersionUID = 5485844129188037454L;
    private int[] data;
    int size;

    public ArrayMapping() {
        this.size = 0;
        this.data = new int[0];
    }

    public ArrayMapping(int[] array, int start, int end) {
        this.data = new int[end - start];
        System.arraycopy(array, start, data, 0, end - start);
        this.size = end - start;
    }

    public ArrayMapping(int start, int end) {
        this.data = IntArrays.newSeq(start, end);
        this.size = end - start;
    }

    public ArrayMapping(int[] array, int start, int end, IntIntFunction fun) {
        this.data = IntArrays.newFrom(array, start, end, fun);
        this.size = end - start;
    }

    public int size() {
        return size;
    }

    public int get(int pos) {
        return data[pos];
    }

    public void add(int value) {
        if (!IntArrays.checkCapacity(data, size + 1)) {
            this.data = IntArrays.ensureCapacity(data, size + 1);
        }
        this.data[size++] = value;
    }

    public void addAll(IntIterator it) {
        while (it.hasNext()) {
            add(it.nextInt());
        }
    }

    @Override
    public void remove(int pos) {
        IntArrays.delete(data, size, pos);
        size--;
    }

    @Override
    public void removeAll(IntIterator it) {
        while (it.hasNext()) {
            remove(it.nextInt());
        }
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public IntIterator iterator() {
        return IntArrays.iterator(data, 0, size);
    }

    public IntIterator iterator(int start, int end) {
        return IntArrays.iterator(data, start, end);
    }

    @Override
    public int[] elements() {
        return data;
    }

    @Override
    public void shuffle() {
        IntArrays.shuffle(data, 0, size, RandomSource.getRandom());
    }

    @Override
    public IntStream stream() {
        return IntArrays.stream(data, 0, size);
    }
}

