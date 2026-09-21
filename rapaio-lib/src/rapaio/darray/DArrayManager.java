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

package rapaio.darray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import rapaio.core.distributions.Distribution;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.manager.base.BaseDArrayManager;
import rapaio.util.Hardware;

public abstract class DArrayManager {

    /**
     * Default number of scalar operations below which an operation runs on the calling thread instead of
     * being split across the manager's thread pool. Handing work to another thread costs tens of microseconds,
     * which is about the time needed to process this many elements with a vectorized kernel.
     */
    public static final int DEFAULT_PARALLEL_THRESHOLD = 1 << 16;

    public static DArrayManager base() {
        return new BaseDArrayManager(Hardware.CORES);
    }

    public static DArrayManager base(int cpuThreads) {
        return new BaseDArrayManager(cpuThreads);
    }

    public static DArrayManager base(int cpuThreads, int parallelThreshold) {
        return new BaseDArrayManager(cpuThreads, parallelThreshold);
    }

    protected final int cpuThreads;
    protected final int parallelThreshold;
    protected final StorageManager storageManager;
    private volatile ExecutorService executor;

    protected DArrayManager(int cpuThreads, StorageManager storageManager) {
        this(cpuThreads, DEFAULT_PARALLEL_THRESHOLD, storageManager);
    }

    protected DArrayManager(int cpuThreads, int parallelThreshold, StorageManager storageManager) {
        if (cpuThreads < 1) {
            throw new IllegalArgumentException("Number of cpu threads must be at least 1 (given: " + cpuThreads + ").");
        }
        if (parallelThreshold < 0) {
            throw new IllegalArgumentException("Parallel threshold must be non-negative (given: " + parallelThreshold + ").");
        }
        this.cpuThreads = cpuThreads;
        this.parallelThreshold = parallelThreshold;
        this.storageManager = storageManager;
    }

    public final int cpuThreads() {
        return cpuThreads;
    }

    /**
     * @return minimum amount of work (in scalar operations) from which operations are executed on multiple threads
     */
    public final int parallelThreshold() {
        return parallelThreshold;
    }

    /**
     * Tells whether an operation involving the given amount of work should be split across threads.
     *
     * @param workSize amount of work, usually the number of elements touched by the operation
     * @return true if the manager has more than one thread and the work reaches the parallel threshold
     */
    public final boolean runParallel(long workSize) {
        return cpuThreads > 1 && workSize >= parallelThreshold;
    }

    /**
     * Runs the given independent tasks and returns after all of them completed.
     * <p>
     * When the amount of work is below {@link #parallelThreshold()}, the manager has a single thread, or there is
     * a single task, the tasks run sequentially on the calling thread. Otherwise they are submitted to the
     * manager's shared thread pool, which is created lazily and uses daemon threads so it never keeps the JVM alive.
     * The pool is a {@link ForkJoinPool}, so a task may itself call this method (nested parallel operations)
     * without risking pool exhaustion.
     * <p>
     * Any exception thrown by a task is propagated to the caller after the remaining tasks finished.
     *
     * @param workSize amount of work, usually the number of elements touched by the whole operation
     * @param tasks    independent units of work
     */
    public final void execute(long workSize, List<? extends Runnable> tasks) {
        if (tasks.isEmpty()) {
            return;
        }
        if (tasks.size() == 1 || !runParallel(workSize)) {
            for (Runnable task : tasks) {
                task.run();
            }
            return;
        }
        ExecutorService pool = executor();
        List<Future<?>> futures = new ArrayList<>(tasks.size());
        for (Runnable task : tasks) {
            futures.add(pool.submit(task));
        }
        RuntimeException failure = null;
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for parallel tasks.", e);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (failure == null) {
                    failure = switch (cause) {
                        case RuntimeException re -> re;
                        case Error err -> throw err;
                        default -> new RuntimeException(cause);
                    };
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private ExecutorService executor() {
        ExecutorService pool = executor;
        if (pool == null) {
            synchronized (this) {
                pool = executor;
                if (pool == null) {
                    pool = new ForkJoinPool(cpuThreads);
                    executor = pool;
                }
            }
        }
        return pool;
    }

    public final StorageManager storageManager() {
        return storageManager;
    }

    public final <N extends Number> DArray<N> scalar(DType<N> dt, byte value) {
        return stride(dt, StrideLayout.of(Shape.of(), 0, new int[0]), storageManager.scalar(dt, value));
    }

    public final <N extends Number> DArray<N> scalar(DType<N> dt, int value) {
        return stride(dt, StrideLayout.of(Shape.of(), 0, new int[0]), storageManager.scalar(dt, value));
    }

    public final <N extends Number> DArray<N> scalar(DType<N> dt, float value) {
        return stride(dt, StrideLayout.of(Shape.of(), 0, new int[0]), storageManager.scalar(dt, value));
    }

    public final <N extends Number> DArray<N> scalar(DType<N> dt, double value) {
        return stride(dt, StrideLayout.of(Shape.of(), 0, new int[0]), storageManager.scalar(dt, value));
    }

    public final <N extends Number> DArray<N> zeros(DType<N> dt, Shape shape) {
        return zeros(dt, shape, Order.defaultOrder());
    }

    public final <N extends Number> DArray<N> zeros(DType<N> dt, Shape shape, Order order) {
        return stride(dt, shape, Order.autoFC(order), storageManager.zeros(dt, shape.size()));
    }

    public final <N extends Number> DArray<N> eye(DType<N> dt, int n) {
        return eye(dt, n, Order.defaultOrder());
    }

    public final <N extends Number> DArray<N> eye(DType<N> dt, int n, Order order) {
        var eye = zeros(dt, Shape.of(n, n), order);
        var v = dt.cast(1);
        for (int i = 0; i < n; i++) {
            eye.set(v, i, i);
        }
        return eye;
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, byte value) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(Order.defaultOrder()), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, int value) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(Order.defaultOrder()), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, float value) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(Order.defaultOrder()), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, double value) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(Order.defaultOrder()), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, byte value, Order askOrder) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(askOrder), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, int value, Order askOrder) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(askOrder), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, float value, Order askOrder) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(askOrder), storage);
    }

    public final <N extends Number> DArray<N> full(DType<N> dt, Shape shape, double value, Order askOrder) {
        var storage = storageManager.zeros(dt, shape.size());
        storage.fill(value, 0, shape.size());
        return stride(dt, shape, Order.autoFC(askOrder), storage);
    }

    public final <N extends Number> DArray<N> seq(DType<N> dt, Shape shape) {
        return seq(dt, shape, Order.defaultOrder());
    }

    public final <N extends Number> DArray<N> seq(DType<N> dt, Shape shape, Order order) {
        return zeros(dt, shape, Order.autoFC(order)).apply_(Order.C, (i, _) -> dt.cast(i));
    }

    public final <N extends Number> DArray<N> random(DType<N> dt, Shape shape, Random random) {
        return random(dt, shape, random, Order.defaultOrder());
    }

    public abstract <N extends Number> DArray<N> random(DType<N> dt, Shape shape, Random random, Order order);

    public final <N extends Number> DArray<N> random(DType<N> dt, Shape shape, Distribution dist, Random random) {
        return random(dt, shape, dist, random, Order.defaultOrder());
    }

    public final <N extends Number> DArray<N> random(DType<N> dt, Shape shape, Distribution dist, Random random, Order order) {
        return zeros(dt, shape, Order.autoFC(order)).apply_(order, (_, _) -> dt.cast(dist.sampleNext(random)));
    }


    public final <N extends Number> DArray<N> stride(DType<N> dt, Shape shape, Order order, Storage storage) {
        return stride(dt, StrideLayout.ofDense(shape, 0, order), storage);
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, Shape shape, Order order, byte... array) {
        return stride(dt, StrideLayout.ofDense(shape, 0, order), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, Shape shape, Order order, int... array) {
        return stride(dt, StrideLayout.ofDense(shape, 0, order), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, float... array) {
        return stride(dt, Shape.of(array.length), Order.defaultOrder(), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, double... array) {
        return stride(dt, Shape.of(array.length), Order.defaultOrder(), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, byte... array) {
        return stride(dt, Shape.of(array.length), Order.defaultOrder(), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, int... array) {
        return stride(dt, Shape.of(array.length), Order.defaultOrder(), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, Shape shape, Order order, float... array) {
        return stride(dt, StrideLayout.ofDense(shape, 0, order), storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, Shape shape, Order order, double... array) {
        return stride(dt, StrideLayout.ofDense(shape, 0, order), storageManager.from(dt, array));
    }

    public abstract <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, Storage storage);

    public final <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, byte[] array) {
        return stride(dt, layout, storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, int[] array) {
        return stride(dt, layout, storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, float[] array) {
        return stride(dt, layout, storageManager.from(dt, array));
    }

    public final <N extends Number> DArray<N> stride(DType<N> dt, StrideLayout layout, double[] array) {
        return stride(dt, layout, storageManager.from(dt, array));
    }

    /**
     * Concatenates multiple DArrays along a new axis.
     * All DArrays must have the same shape. The position of the new axis is between 0 (inclusive)
     * and the number of dimensions (inclusive).
     * <p>
     * The new DArray will have an additional dimension, which is the dimension created during stacking and
     * will have size equal with the number of the stacked arrays.
     * <p>
     * The new DArray with be stored with default order.
     *
     * @param axis    index of the new dimension
     * @param nArrays DArrays to concatenate
     * @return new DArray with concatenated data
     */
    public final <N extends Number> DArray<N> stack(DType<N> dt, int axis, Collection<? extends DArray<?>> nArrays) {
        return stack(dt, Order.defaultOrder(), axis, nArrays);
    }

    public final <N extends Number> DArray<N> stack(DType<N> dt, Order order, int axis, Collection<? extends DArray<?>> nArrays) {
        var nArrayList = nArrays.stream().toList();
        for (int i = 1; i < nArrayList.size(); i++) {
            if (!nArrayList.get(i - 1).shape().equals(nArrayList.get(i).shape())) {
                throw new IllegalArgumentException("DArrays are not valid for stack, they have to have the same dimensions.");
            }
        }
        int[] newDims = new int[nArrayList.getFirst().rank() + 1];
        int i = 0;
        for (; i < axis; i++) {
            newDims[i] = nArrayList.getFirst().shape().dim(i);
        }
        for (; i < nArrayList.getFirst().rank(); i++) {
            newDims[i + 1] = nArrayList.getFirst().shape().dim(i);
        }
        newDims[axis] = nArrayList.size();
        var result = zeros(dt, Shape.of(newDims), order);
        var slices = result.chunk(axis, true, 1);
        i = 0;
        for (; i < nArrayList.size(); i++) {
            var it1 = slices.get(i).squeeze(axis).ptrIterator(Order.defaultOrder());
            var it2 = nArrayList.get(i).ptrIterator(Order.defaultOrder());
            while (it1.hasNext() && it2.hasNext()) {
                slices.get(i).ptrSet(it1.nextInt(), dt.cast(nArrayList.get(i).ptrGet(it2.nextInt())));
            }
        }
        return result;
    }

    public final <N extends Number> DArray<N> cat(DType<N> dt, int axis, Collection<? extends DArray<?>> nArrays) {
        return cat(dt, Order.defaultOrder(), axis, nArrays);
    }

    public final <N extends Number> DArray<N> cat(DType<N> dt, Order order, int axis, Collection<? extends DArray<?>> nArrays) {
        var nArrayList = nArrays.stream().toList();
        DArrayManager.validateForConcatenation(axis, nArrayList.stream().map(t -> t.shape().dims()).collect(Collectors.toList()));

        int newDim = nArrayList.stream().mapToInt(nArray -> nArray.layout().shape().dim(axis)).sum();
        DArray<?> first = nArrayList.getFirst();
        int[] newDims = Arrays.copyOf(first.shape().dims(), first.rank());
        newDims[axis] = newDim;
        var result = zeros(dt, Shape.of(newDims), order);

        int start = 0;
        for (DArray<?> array : nArrays) {
            int end = start + array.shape().dim(axis);
            var dst = result.narrow(axis, true, start, end);

            var it1 = array.ptrIterator(Order.defaultOrder());
            var it2 = dst.ptrIterator(Order.defaultOrder());

            while (it1.hasNext() && it2.hasNext()) {
                dst.ptrSet(it2.nextInt(), dt.cast(array.ptrGet(it1.nextInt())));
            }
            start = end;
        }
        return result;
    }

    private static void validateForConcatenation(int axis, List<int[]> dims) {
        for (int i = 1; i < dims.size(); i++) {
            int[] dimsPrev = dims.get(i - 1);
            int[] dimsNext = dims.get(i);
            for (int j = 0; j < dimsPrev.length; j++) {
                if (j != axis && dimsNext[j] != dimsPrev[j]) {
                    throw new IllegalArgumentException("DArrays are not valid for concatenation");
                }
            }
        }
    }
}
