/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.darray.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;

/**
 * Tests for the shared executor of {@link DArrayManager} and for the operations that dispatch work through it.
 */
public class DArrayManagerExecutorTest {

    /**
     * Managers covering the three dispatch modes: single thread (always inline), default threshold
     * (inline for the small arrays used here) and zero threshold (every operation goes through the pool).
     */
    static Stream<DArrayManager> managers() {
        return Stream.of(
                DArrayManager.base(1),
                DArrayManager.base(4),
                DArrayManager.base(4, 0)
        );
    }

    @Test
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> DArrayManager.base(0));
        assertThrows(IllegalArgumentException.class, () -> DArrayManager.base(2, -1));
        assertEquals(DArrayManager.DEFAULT_PARALLEL_THRESHOLD, DArrayManager.base().parallelThreshold());
        assertEquals(10, DArrayManager.base(2, 10).parallelThreshold());
    }

    @Test
    void runParallelHonoursThreadsAndThreshold() {
        assertFalse(DArrayManager.base(1, 0).runParallel(Long.MAX_VALUE));
        DArrayManager dm = DArrayManager.base(2, 100);
        assertFalse(dm.runParallel(99));
        assertTrue(dm.runParallel(100));
        assertTrue(DArrayManager.base(2, 0).runParallel(0));
    }

    @Test
    void executeRunsInlineBelowThresholdOrWithSingleThread() {
        Thread caller = Thread.currentThread();
        Set<Thread> seen = ConcurrentHashMap.newKeySet();
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            tasks.add(() -> seen.add(Thread.currentThread()));
        }

        DArrayManager.base(4, 1000).execute(999, tasks);
        assertEquals(Set.of(caller), seen);

        seen.clear();
        DArrayManager.base(1, 0).execute(1_000_000, tasks);
        assertEquals(Set.of(caller), seen);

        // a single task never pays for the pool either
        seen.clear();
        DArrayManager.base(4, 0).execute(1_000_000, List.of(() -> seen.add(Thread.currentThread())));
        assertEquals(Set.of(caller), seen);
    }

    @Test
    void executeRunsAllTasksOnPoolAboveThreshold() {
        DArrayManager dm = DArrayManager.base(4, 0);
        AtomicInteger counter = new AtomicInteger();
        Set<Thread> seen = ConcurrentHashMap.newKeySet();
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 64; i++) {
            tasks.add(() -> {
                seen.add(Thread.currentThread());
                counter.incrementAndGet();
            });
        }
        dm.execute(1, tasks);
        assertEquals(64, counter.get());
        assertFalse(seen.contains(Thread.currentThread()));
        // pool threads are daemons, so a manager never keeps the JVM alive
        seen.forEach(t -> assertTrue(t.isDaemon()));
        // the pool is bounded by cpuThreads (ForkJoinPool may add a few compensation threads while tasks block)
        assertTrue(seen.size() <= 4 + 2, "threads used: " + seen.size());
    }

    @Test
    void executePropagatesTaskFailures() {
        DArrayManager dm = DArrayManager.base(4, 0);
        AtomicInteger completed = new AtomicInteger();
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            int id = i;
            tasks.add(() -> {
                if (id == 5) {
                    throw new IllegalStateException("task 5 failed");
                }
                completed.incrementAndGet();
            });
        }
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> dm.execute(1, tasks));
        // ForkJoinPool may re-wrap the exception in a fresh instance of the same type to keep the caller's stack
        assertTrue(ex.getMessage().contains("task 5 failed"), ex.getMessage());
        // the failure does not abandon the other tasks, and the caller is not left waiting forever
        assertEquals(15, completed.get());
    }

    @Test
    void executeSupportsNestedParallelCalls() {
        DArrayManager dm = DArrayManager.base(2, 0);
        AtomicInteger counter = new AtomicInteger();
        List<Runnable> outer = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            outer.add(() -> {
                List<Runnable> inner = new ArrayList<>();
                for (int j = 0; j < 8; j++) {
                    inner.add(counter::incrementAndGet);
                }
                dm.execute(1, inner);
            });
        }
        // with a plain fixed pool of 2 threads this would deadlock: outer tasks block waiting for inner ones
        dm.execute(1, outer);
        assertEquals(64, counter.get());
    }

    /**
     * Regression: unary1d_ computed the number of row groups from the size of the reduced axis instead of the
     * number of rows, so a (1000, 2) array processed along axis 1 only touched the first 64 rows.
     */
    @ParameterizedTest
    @MethodSource("managers")
    void softmax1dTouchesEveryRow(DArrayManager dm) {
        Random random = new Random(1);
        DArray<Double> x = dm.random(DType.DOUBLE, Shape.of(1000, 2), random);
        DArray<Double> s = x.softmax1d(1);
        for (int i = 0; i < 1000; i++) {
            double e0 = Math.exp(x.getDouble(i, 0));
            double e1 = Math.exp(x.getDouble(i, 1));
            assertEquals(e0 / (e0 + e1), s.getDouble(i, 0), 1e-12, "row " + i);
            assertEquals(e1 / (e0 + e1), s.getDouble(i, 1), 1e-12, "row " + i);
        }
        // the transposed case: many rows, few columns along axis 0
        DArray<Double> t = x.t().softmax1d(0);
        assertTrue(t.t().deepEquals(s, 1e-12));
    }

    /**
     * Regression: var1d with a precomputed mean advanced the mean iterator from inside the parallel tasks,
     * so concurrent chunks paired rows with the wrong means.
     */
    @ParameterizedTest
    @MethodSource("managers")
    void var1dWithMeanMatchesVar1dWithoutMean(DArrayManager dm) {
        Random random = new Random(2);
        DArray<Double> x = dm.random(DType.DOUBLE, Shape.of(2000, 3), random);
        // shift rows so that pairing a row with another row's mean gives a visibly different variance
        x.apply_(Order.C, (i, p) -> x.ptrGetDouble(p) + (i / 3) * 10.0);
        DArray<Double> mean = x.mean1d(1);
        DArray<Double> expected = x.var1d(1, 1);
        DArray<Double> actual = x.var1d(1, 1, mean);
        assertTrue(expected.deepEquals(actual, 1e-9));
    }

    /**
     * All row-wise operations that use the executor must give the same result whether they run inline or
     * on the pool.
     */
    @Test
    void parallelAndSerialResultsAgree() {
        Random random = new Random(3);
        DArrayManager serial = DArrayManager.base(1);
        DArrayManager parallel = DArrayManager.base(4, 0);
        DArray<Double> xs = serial.random(DType.DOUBLE, Shape.of(37, 53), random);
        DArray<Double> xp = parallel.zeros(DType.DOUBLE, xs.shape());
        xs.copyTo(xp);
        assertTrue(xs.deepEquals(xp));

        for (int axis = 0; axis < 2; axis++) {
            assertTrue(xs.sum1d(axis).deepEquals(xp.sum1d(axis), 1e-12));
            assertTrue(xs.var1d(axis, 1).deepEquals(xp.var1d(axis, 1), 1e-12));
            assertTrue(xs.argmax1d(axis, false).deepEquals(xp.argmax1d(axis, false)));
            assertTrue(xs.argmin1d(axis, false).deepEquals(xp.argmin1d(axis, false)));
            assertTrue(xs.softmax1d(axis).deepEquals(xp.softmax1d(axis), 1e-12));
        }
        assertTrue(xs.sumTo(Shape.of(53), false).deepEquals(xp.sumTo(Shape.of(53), false), 1e-12));

        DArray<Double> ys = serial.random(DType.DOUBLE, Shape.of(53, 29), random);
        DArray<Double> yp = parallel.zeros(DType.DOUBLE, ys.shape());
        ys.copyTo(yp);
        // the parallel matrix product accumulates in k-blocks, so summation order differs slightly
        assertTrue(xs.mm(ys).deepEquals(xp.mm(yp), 1e-9));

        DArray<Double> big = parallel.zeros(DType.DOUBLE, Shape.of(300, 400));
        serial.random(DType.DOUBLE, big.shape(), random).copyTo(big);
        assertTrue(big.copy().deepEquals(big));
    }
}
