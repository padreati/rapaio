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

package rapaio.experiment.math.tensor.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StorageTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    interface StorageProvider<N extends Number, S extends Storage<N>> {

        N value(int value);

        N randomValue(Random random);

        Storage<N> zeros(int size);

        Storage<N> fill(int size, int value);

        Storage<N> seq(int size);

        Storage<N> wrap(int[] array);

        Storage<N> random(int size, Random random);
    }

    static class DStorageProvider implements StorageProvider<Double, DStorage> {

        @Override
        public Double value(int value) {
            return (double) value;
        }

        @Override
        public Double randomValue(Random random) {
            return random.nextDouble();
        }

        @Override
        public Storage<Double> zeros(int size) {
            return DStorage.zeros(size);
        }

        @Override
        public Storage<Double> fill(int size, int value) {
            return DStorage.fill(size, value);
        }

        @Override
        public Storage<Double> seq(int size) {
            return DStorage.seq(0, size);
        }

        @Override
        public Storage<Double> random(int size, Random random) {
            return DStorage.random(size, random);
        }

        @Override
        public Storage<Double> wrap(int[] array) {
            double[] values = new double[array.length];
            for (int i = 0; i < array.length; i++) {
                values[i] = array[i];
            }
            return DStorage.wrap(values);
        }
    }

    static class FStorageProvider implements StorageProvider<Float, FStorage> {

        @Override
        public Float value(int value) {
            return (float) value;
        }

        @Override
        public Float randomValue(Random random) {
            return random.nextFloat();
        }

        @Override
        public Storage<Float> zeros(int size) {
            return FStorage.zeros(size);
        }

        @Override
        public Storage<Float> fill(int size, int value) {
            return FStorage.fill(size, value);
        }

        @Override
        public Storage<Float> seq(int size) {
            return FStorage.seq(0, size);
        }

        @Override
        public Storage<Float> random(int size, Random random) {
            return FStorage.random(size, random);
        }

        @Override
        public Storage<Float> wrap(int[] array) {
            float[] values = new float[array.length];
            for (int i = 0; i < array.length; i++) {
                values[i] = array[i];
            }
            return FStorage.wrap(values);
        }
    }

    @Test
    void genericTestRunner() {
        genericTestSuite(new DStorageProvider());
        genericTestSuite(new FStorageProvider());
    }

    <N extends Number, S extends Storage<N>> void genericTestSuite(StorageProvider<N, S> storageProvider) {
        testBuilder(storageProvider);
        testFill(storageProvider);
        testAdd(storageProvider);
        testSub(storageProvider);
        testMul(storageProvider);
        testDiv(storageProvider);
        testMinArgMin(storageProvider);
    }

    <N extends Number, S extends Storage<N>> void testBuilder(StorageProvider<N, S> provider) {

        Storage<N> storage = provider.zeros(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(0), storage.get(i));
        }
        assertEquals(10, storage.size());

        storage = provider.seq(10);
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(i), storage.get(i));
        }
        assertEquals(10, storage.size());

        storage = provider.fill(10, 3);
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(3), storage.get(i));
        }
        assertEquals(10, storage.size());

        int[] values = new int[] {2, 5, 7, 9};
        storage = provider.wrap(values);
        assertEquals(values.length, storage.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(provider.value(values[i]), storage.get(i));
        }

        Random r = new Random(42);
        storage = provider.random(10, r);
        r = new Random(42);
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.randomValue(r), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testFill(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.zeros(10);
        storage.fill(0, 10, provider.value(100));
        storage.fill(1, 2, provider.value(10));
        storage.fill(6, 2, provider.value(1));

        int[] expected = new int[] {100, 10, 10, 100, 100, 100, 1, 1, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testAdd(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.zeros(10);
        storage.add(0, 10, provider.value(100));
        storage.add(1, 2, provider.value(10));
        storage.add(6, 2, provider.value(1));

        int[] expected = new int[] {100, 110, 110, 100, 100, 100, 101, 101, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testSub(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.zeros(10);
        storage.sub(0, 10, provider.value(100));
        storage.sub(1, 2, provider.value(10));
        storage.sub(6, 2, provider.value(1));

        int[] expected = new int[] {-100, -110, -110, -100, -100, -100, -101, -101, -100, -100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testMul(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.fill(10, 1);
        storage.mul(0, 10, provider.value(100));
        storage.mul(1, 2, provider.value(10));
        storage.mul(6, 2, provider.value(1));

        int[] expected = new int[] {100, 1000, 1000, 100, 100, 100, 100, 100, 100, 100};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testDiv(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.fill(10, 64);
        storage.div(0, 10, provider.value(2));
        storage.div(1, 2, provider.value(2));
        storage.div(6, 2, provider.value(4));

        int[] expected = new int[] {32, 16, 16, 32, 32, 32, 8, 8, 32, 32};
        for (int i = 0; i < 10; i++) {
            assertEquals(provider.value(expected[i]), storage.get(i));
        }
    }

    <N extends Number, S extends Storage<N>> void testMinArgMin(StorageProvider<N, S> provider) {
        Storage<N> storage = provider.random(100, random);
        N min = storage.min(0, 100);
        int index = storage.argMin(0, 100);
        assertEquals(min, storage.get(index));
        assertEquals(-1, storage.argMin(0, 0));
    }
}
