/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.nn;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rapaio.core.distributions.Distribution;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.tensors.Variable;
import rapaio.nn.tensors.shape.Cat;

/**
 * Context manager for tensor computations. This class allows one to create tensors and DArrays which
 * are tensor values and gradients. It also provides a stable random source, a data type for
 * intermediate computations and an execution service for parallel computations.
 */
public final class TensorManager implements AutoCloseable {

    public static TensorManager ofFloat() {
        return new TensorManager(DType.FLOAT,
                Math.ceilDiv(Runtime.getRuntime().availableProcessors(), 2),
                Math.floorDiv(Runtime.getRuntime().availableProcessors(), 2));
    }

    public static TensorManager ofDouble() {
        return new TensorManager(DType.DOUBLE,
                Math.ceilDiv(Runtime.getRuntime().availableProcessors(), 2),
                Math.floorDiv(Runtime.getRuntime().availableProcessors(), 2));
    }

    private final DType<?> dt;
    private final Random random;

    private final DArrayManager arrayManager;
    private final int outerThreads;
    private final int innerThreads;

    private final ExecutorService outerExecutor;

    private TensorManager(DType<?> dt, int outerThreads, int innerThreads) {
        this.dt = dt;
        this.random = new Random();
        this.arrayManager = DArrayManager.base();
        this.outerThreads = outerThreads;
        this.innerThreads = innerThreads;

        this.outerExecutor = Executors.newFixedThreadPool(outerThreads);
    }

    public TensorManager seed(long seed) {
        random.setSeed(seed);
        return this;
    }

    public Random random() {
        return random;
    }

    public DType<?> dt() {
        return dt;
    }

    public DArrayManager arrayManager() {
        return arrayManager;
    }

    public ExecutorService outerExecutor() {
        return outerExecutor;
    }

    public int outerThreads() {
        return outerThreads;
    }

    public int innerThreads() {
        return innerThreads;
    }

    @Override
    public void close() {
        outerExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!outerExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                outerExecutor.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!outerExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            outerExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    // tensor and array creation

    public Variable var(DArray<?> value) {
        return new Variable(this, value);
    }

    public Variable var() {
        return new Variable(this);
    }

    public Variable scalarTensor(double value) {
        return new Variable(this, arrayManager.scalar(dt, value));
    }

    public Variable zerosTensor(Shape shape) {
        return new Variable(this, arrayManager.zeros(dt, shape));
    }

    public Variable fullTensor(Shape shape, double fill) {
        return new Variable(this, arrayManager.full(dt, shape, fill));
    }

    public Variable randomTensor(Shape shape) {
        return new Variable(this, arrayManager.random(dt, shape, random));
    }

    public Variable randomTensor(Shape shape, Distribution distribution) {
        return new Variable(this, arrayManager.random(dt, shape, distribution, random, Order.defaultOrder()));
    }

    public Variable randomTensor(Shape shape, Distribution distribution, Order askOrder) {
        return new Variable(this, arrayManager.random(dt, shape, distribution, random, askOrder));
    }

    public Variable seqTensor(Shape shape) {
        return new Variable(this, arrayManager.seq(dt, shape));
    }

    public Variable strideTensor(Shape shape, byte... values) {
        return new Variable(this, arrayManager.stride(dt, shape, Order.defaultOrder(), values));
    }

    public Variable strideTensor(Shape shape, int... values) {
        return new Variable(this, arrayManager.stride(dt, shape, Order.defaultOrder(), values));
    }

    public Variable strideTensor(Shape shape, double... values) {
        return new Variable(this, arrayManager.stride(dt, shape, Order.defaultOrder(), values));
    }

    public Variable strideTensor(Shape shape, float... values) {
        return new Variable(this, arrayManager.stride(dt, shape, Order.defaultOrder(), values));
    }


    public DArray<?> scalarArray(double value) {
        return arrayManager.scalar(dt, value);
    }

    public DArray<?> zerosArray(Shape shape) {
        return arrayManager.zeros(dt, shape);
    }

    public DArray<?> zerosArray(DType<?> dt, Shape shape) {
        return arrayManager.zeros(dt, shape);
    }

    public DArray<?> fullArray(Shape shape, double fill) {
        return arrayManager.full(dt, shape, fill);
    }

    public DArray<?> randomArray(Shape shape) {
        return arrayManager.random(dt, shape, random);
    }

    public DArray<?> randomArray(Shape shape, Distribution distribution) {
        return arrayManager.random(dt, shape, distribution, random, Order.defaultOrder());
    }

    public DArray<?> randomArray(Shape shape, Distribution distribution, Order askOrder) {
        return arrayManager.random(dt, shape, distribution, random, askOrder);
    }

    public DArray<?> seqArray(Shape shape) {
        return arrayManager.seq(dt, shape);
    }

    public DArray<?> strideArray(Shape shape, byte... values) {
        return arrayManager.stride(dt, shape, Order.defaultOrder(), values);
    }

    public DArray<?> strideArray(Shape shape, int... values) {
        return arrayManager.stride(dt, shape, Order.defaultOrder(), values);
    }

    public DArray<?> strideArray(Shape shape, double... values) {
        return arrayManager.stride(dt, shape, Order.defaultOrder(), values);
    }

    public DArray<?> strideArray(Shape shape, float... values) {
        return arrayManager.stride(dt, shape, Order.defaultOrder(), values);
    }

    public Tensor cat(int axis, Tensor... tensors) {
        return new Cat(this, axis, tensors);
    }
}
