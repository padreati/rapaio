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

package rapaio.math.tensor.storage.array;

import java.util.Arrays;

import rapaio.math.tensor.storage.ByteStorage;

public final class ByteArrayStorage extends ByteStorage {

    private final byte[] array;

    public ByteArrayStorage(byte[] array) {
        this.array = array;
    }

    @Override
    public int size() {
        return array.length;
    }

    public byte getByte(int ptr) {
        return array[ptr];
    }

    public void setByte(int ptr, byte v) {
        array[ptr] = v;
    }

    @Override
    public void incByte(int ptr, byte value) {
        array[ptr] += value;
    }

    @Override
    public void fillByte(byte value) {
        Arrays.fill(array, value);
    }
}
