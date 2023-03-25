/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.tensor.storage.array.ArrayStorageFactory;
import rapaio.util.collection.IntArrays;

public class StorageTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    abstract static class StorageProvider<N extends Number, S extends Storage<N, S>> {

        protected final StorageFactory storageFactory;

        public StorageProvider(StorageFactory storageFactory) {
            this.storageFactory = storageFactory;
        }

        abstract N value(int value);

        abstract N randomValue(Random random);

        abstract boolean isNaN(N value);

        abstract S zeros(int size);

        abstract S fill(int size, int value);

        abstract S seq(int size);

        abstract S wrap(int[] array);

        abstract S random(int size, Random random);
    }

    static class DStorageProvider extends StorageProvider<Double, DStorage> {

        DStorageProvider(StorageFactory storageFactory) {
            super(storageFactory);
        }

        @Override
        public Double value(int value) {
            return (double) value;
        }

        @Override
        public Double randomValue(Random random) {
            return random.nextDouble();
        }

        @Override
        public boolean isNaN(Double value) {
            return Double.isNaN(value);
        }

        @Override
        public DStorage zeros(int size) {
            return storageFactory.ofDoubleZeros(size);
        }

        @Override
        public DStorage fill(int size, int value) {
            return storageFactory.ofDoubleFill(size, value);
        }

        @Override
        public DStorage seq(int size) {
            return storageFactory.ofDoubleSeq(0, size);
        }

        @Override
        public DStorage random(int size, Random random) {
            return storageFactory.ofDoubleRandom(size, random);
        }

        @Override
        public DStorage wrap(int[] array) {
            double[] values = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                values[i] = array[i];
            }
            return storageFactory.ofDoubleWrap(values);
        }
    }

    static class FStorageProvider extends StorageProvider<Float, FStorage> {

        public FStorageProvider(StorageFactory storageFactory) {
            super(storageFactory);
        }

        @Override
        public Float value(int value) {
            return (float) value;
        }

        @Override
        public Float randomValue(Random random) {
            return random.nextFloat();
        }

        @Override
        public boolean isNaN(Float value) {
            return Float.isNaN(value);
        }

        @Override
        public FStorage zeros(int size) {
            return storageFactory.ofFloatZeros(size);
        }

        @Override
        public FStorage fill(int size, int value) {
            return storageFactory.ofFloatFill(size, value);
        }

        @Override
        public FStorage seq(int size) {
            return storageFactory.ofFloatSeq(0, size);
        }

        @Override
        public FStorage random(int size, Random random) {
            return storageFactory.ofFloatRandom(size, random);
        }

        @Override
        public FStorage wrap(int[] array) {
            float[] values = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                values[i] = array[i];
            }
            return storageFactory.ofFloatWrap(values);
        }
    }

    @Test
    void genericTestRunner() {
        genericTestSuite(new DStorageProvider(new ArrayStorageFactory()));
        genericTestSuite(new FStorageProvider(new ArrayStorageFactory()));
    }

    <N extends Number, S extends Storage<N, S>> void genericTestSuite(StorageProvider<N, S> storageProvider) {
        testBuilder(storageProvider);
        testCopy(storageProvider);
        testFill(storageProvider);
        testReverse(storageProvider);
        testAdd(storageProvider);
        testSub(storageProvider);
        testMul(storageProvider);
        testDiv(storageProvider);
        testMinArgMin(storageProvider);
    }

    <N extends Number, S extends Storage<N, S>> void testBuilder(StorageProvider<N, S> provider) {

        int len = random.nextInt(100) + 2;
        Storage<N, S> storage = provider.zeros(len);
        for (int i = 0; i < len; i++) {
            assertEquals(provider.value(0), storage.getValue(i));
        }
        assertEquals(len, storage.size());

        storage = provider.seq(len);
        for (int i = 0; i < len; i++) {
            assertEquals(provider.value(i), storage.getValue(i));
        }
        assertEquals(len, storage.size());

        storage = provider.fill(len, 3);
        for (int i = 0; i < len; i++) {
            assertEquals(provider.value(3), storage.getValue(i));
        }
        assertEquals(len, storage.size());

        int[] values = new int[] {2, 5, 7, 9};
        storage = provider.wrap(values);
        assertEquals(values.length, storage.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(provider.value(values[i]), storage.getValue(i));
        }

        Random r = new Random(42);
        storage = provider.random(len, r);
        r = new Random(42);
        for (int i = 0; i < len; i++) {
            assertEquals(provider.randomValue(r), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testCopy(StorageProvider<N, S> provider) {
        var storage = provider.seq(10);
        var copy = storage.copy();

        // test for the same content
        assertEquals(storage.size(), copy.size());
        for (int i = 0; i < storage.size(); i++) {
            assertEquals(storage.getValue(i), copy.getValue(i));
        }

        // test for copied data
        storage.fillValue(0, storage.size(), provider.value(-1));
        for (int i = 0; i < storage.size(); i++) {
            assertNotEquals(storage.getValue(i), copy.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testFill(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.zeros(10);
        storage.fillValue(0, 10, provider.value(100));
        storage.fillValue(1, 2, provider.value(10));
        storage.fillValue(6, 2, provider.value(1));

        int[] expected = new int[] {100, 10, 10, 100, 100, 100, 1, 1, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testAdd(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.zeros(10);
        storage.addValue(0, 10, provider.value(100));
        storage.addValue(1, 2, provider.value(10));
        storage.addValue(6, 2, provider.value(1));

        int[] expected = new int[] {100, 110, 110, 100, 100, 100, 101, 101, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testSub(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.zeros(10);
        storage.subValue(0, 10, provider.value(100));
        storage.subValue(1, 2, provider.value(10));
        storage.subValue(6, 2, provider.value(1));

        int[] expected = new int[] {-100, -110, -110, -100, -100, -100, -101, -101, -100, -100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testMul(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.fill(10, 1);
        storage.mulValue(0, 10, provider.value(100));
        storage.mulValue(1, 2, provider.value(10));
        storage.mulValue(6, 2, provider.value(1));

        int[] expected = new int[] {100, 1000, 1000, 100, 100, 100, 100, 100, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testDiv(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.fill(10, 64);
        storage.divValue(0, 10, provider.value(2));
        storage.divValue(1, 2, provider.value(2));
        storage.divValue(6, 2, provider.value(4));

        int[] expected = new int[] {32, 16, 16, 32, 32, 32, 8, 8, 32, 32};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.getValue(i));
        }
    }

    <N extends Number, S extends Storage<N, S>> void testMinArgMin(StorageProvider<N, S> provider) {
        Storage<N, S> storage = provider.random(100, random);
        N min = storage.minValue(0, 100);
        int index = storage.argMin(0, 100);
        assertEquals(min, storage.getValue(index));
        assertEquals(-1, storage.argMin(0, 0));

        assertTrue(provider.isNaN(storage.minValue(0, 0)));
        assertEquals(-1, storage.argMin(0, -1));
    }

    <N extends Number, S extends Storage<N, S>> void testReverse(StorageProvider<N, S> provider) {
        int[] array = IntArrays.newSeq(10_123);
        IntArrays.shuffle(array, random);

        Storage<N, S> storage = provider.wrap(IntArrays.copy(array));
        storage.reverse(0, storage.size());
        IntArrays.reverse(array);

        for (int i = 0; i < array.length; i++) {
            assertEquals(provider.value(array[i]), storage.getValue(i));
        }
    }
}
