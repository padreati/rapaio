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

import rapaio.core.distributions.Distribution;
import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.NArrayManager;
import rapaio.narray.Order;
import rapaio.narray.Shape;

public final class TensorManager {

    public static TensorManager ofFloat() {
        return new TensorManager(DType.FLOAT, Runtime.getRuntime().availableProcessors());
    }

    public static TensorManager ofDouble() {
        return new TensorManager(DType.DOUBLE, Runtime.getRuntime().availableProcessors());
    }

    private final DType<?> dt;
    private final NArrayManager.OfType<?> arrayManager;
    private final int threads;

    private TensorManager(DType<?> dt, int threads) {
        this.dt = dt;
        this.arrayManager = NArrayManager.base().ofType(dt);
        this.threads = threads;
    }

    public DType<?> dtype() {
        return dt;
    }

    public int threads() {
        return threads;
    }

    public Variable var(NArray<?> value) {
        return new Variable(this, value);
    }

    public Variable var() {
        return new Variable(this);
    }

    public Variable scalarTensor(double value) {
        return new Variable(this, arrayManager.scalar(value));
    }

    public Variable zerosTensor(Shape shape) {
        return new Variable(this, arrayManager.zeros(shape));
    }

    public Variable fullTensor(Shape shape, double fill) {
        return new Variable(this, arrayManager.full(shape, fill));
    }

    public Variable randomTensor(Shape shape, Random random) {
        return new Variable(this, arrayManager.random(shape, random));
    }

    public Variable randomTensor(Shape shape, Distribution distribution, Random random) {
        return new Variable(this, arrayManager.random(shape, distribution, random, Order.defaultOrder()));
    }

    public Variable randomTensor(Shape shape, Distribution distribution, Random random, Order askOrder) {
        return new Variable(this, arrayManager.random(shape, distribution, random, askOrder));
    }

    public Variable seqTensor(Shape shape) {
        return new Variable(this, arrayManager.seq(shape));
    }

    public Variable strideTensor(Shape shape, double... values) {
        return new Variable(this, arrayManager.stride(shape, values));
    }

    public Variable strideTensor(Shape shape, int... values) {
        return new Variable(this, arrayManager.stride(shape, values));
    }

    public Variable strideTensor(Shape shape, float... values) {
        return new Variable(this, arrayManager.stride(shape, values));
    }


    public NArray<?> scalarArray(double value) {
        return arrayManager.scalar(value);
    }

    public NArray<?> zerosArray(Shape shape) {
        return arrayManager.zeros(shape);
    }

    public NArray<?> fullArray(Shape shape, double fill) {
        return arrayManager.full(shape, fill);
    }

    public NArray<?> randomArray(Shape shape, Random random) {
        return arrayManager.random(shape, random);
    }

    public NArray<?> randomArray(Shape shape, Distribution distribution, Random random) {
        return arrayManager.random(shape, distribution, random, Order.defaultOrder());
    }

    public NArray<?> randomArray(Shape shape, Distribution distribution, Random random, Order askOrder) {
        return arrayManager.random(shape, distribution, random, askOrder);
    }

    public NArray<?> seqArray(Shape shape) {
        return arrayManager.seq(shape);
    }

    public NArray<?> strideArray(Shape shape, double... values) {
        return arrayManager.stride(shape, values);
    }

    public NArray<?> strideArray(Shape shape, int... values) {
        return arrayManager.stride(shape, values);
    }

    public NArray<?> strideArray(Shape shape, float... values) {
        return arrayManager.stride(shape, values);
    }
}
