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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.stream.IntStream;

import rapaio.data.Mapping;
import rapaio.util.IntIterator;
import rapaio.util.function.Int2IntFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/27/18.
 */
public final class ArrayMapping implements Mapping {

    @Serial
    private static final long serialVersionUID = 5485844129188037454L;
    private final ArrayList<Integer> data;

    public ArrayMapping() {
        this.data = new ArrayList<>();
    }

    public ArrayMapping(int[] array, int start, int end) {
        this();
        for (int i = start; i < end; i++) {
            data.add(array[i]);
        }
    }

    public ArrayMapping(int start, int end) {
        this();
        for (int i = start; i < end; i++) {
            data.add(i);
        }
    }

    public ArrayMapping(int[] array, int start, int end, Int2IntFunction fun) {
        this();
        for (int i = start; i < end; i++) {
            data.add(fun.applyAsInt(array[i]));
        }
    }

    public int size() {
        return data.size();
    }

    public int get(int pos) {
        return data.get(pos);
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
        data.remove(pos);
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
        return new IntIterator() {
            private final Iterator<Integer> it = data.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public int nextInt() {
                return it.next();
            }
        };
    }

    @Override
    public int[] elements() {
        return data.stream().mapToInt(v -> v).toArray();
    }

    @Override
    public void shuffle(Random random) {
        Collections.shuffle(data, random);
    }

    @Override
    public IntStream stream() {
        return data.stream().mapToInt(v -> v);
    }
}

