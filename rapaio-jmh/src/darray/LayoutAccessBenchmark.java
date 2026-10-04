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

package darray;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.Simd;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.iterators.TandemStrideLoopDescriptor;
import rapaio.darray.layout.StrideLayout;

/**
 * Cost of the per-operation layout bookkeeping: the defensive copies handed out by {@code Shape.dims()} and
 * {@code StrideLayout.strides()}, and the operations which pay for them.
 * <p>
 * The copies scale with the rank, not with the number of elements, so the arrays here are deliberately tiny. That
 * is where a fixed per-operation cost is observable at all; on a large array it is swamped by the element work and
 * by memory bandwidth, and the measurement turns into noise rather than into a smaller effect.
 * <p>
 * Read {@code gc.alloc.rate.norm}, the bytes allocated per operation, as the primary result. It is deterministic,
 * while the timings of operations this short carry a wide error bar.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class LayoutAccessBenchmark {

    private static final DArrayManager dm = DArrayManager.base();
    private static final DType<Double> dt = DType.DOUBLE;

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        /** Rank of the array, which is what the defensive copies scale with. */
        @Param({"2", "4", "8"})
        private int rank;

        /** Elements per axis, kept small so that the fixed per-operation cost is not swamped by element work. */
        @Param({"2"})
        private int dim;

        private DArray<Double> x;
        private DArray<Double> y;
        /** Transposed view: not storage-contiguous, so it is walked by {@link rapaio.darray.iterators.StridePointerIterator}. */
        private DArray<Double> transposed;
        /**
         * Carries a unit axis at position 0, which {@code expand} and {@code squeeze} need. It is built by stretching
         * an array of {@code rank - 1} axes, so that the stretched form has exactly {@code rank} axes: stretching a
         * rank 8 array would ask for a 9 axis shape, which {@link Shape} rejects.
         */
        private DArray<Double> stretched;
        private StrideLayout layout;
        private StrideLayout transposedLayout;
        private int[] starts;
        private int[] ends;
        private int[] permutation;
        private Shape flatShape;
        private Shape reduceShape;

        @Setup
        public void setup() {
            int[] dims = new int[rank];
            for (int i = 0; i < rank; i++) {
                dims[i] = dim;
            }
            x = dm.seq(dt, Shape.of(dims));
            y = dm.seq(dt, Shape.of(dims));
            transposed = x.t();
            stretched = dm.seq(dt, Shape.of(Arrays.copyOf(dims, rank - 1))).stretch(0);
            layout = (StrideLayout) x.layout();
            transposedLayout = (StrideLayout) transposed.layout();
            starts = new int[rank];
            ends = new int[rank];
            for (int i = 0; i < rank; i++) {
                ends[i] = Math.max(1, dim - 1);
            }
            permutation = new int[rank];
            for (int i = 0; i < rank; i++) {
                permutation[i] = rank - 1 - i;
            }
            flatShape = Shape.of(x.size());
            // reduce over the innermost axis only, so the reduceOn path splits dims and strides in two
            reduceShape = Shape.of(dim);
        }
    }

    // ---- the getters themselves ----

    @Benchmark
    public void dimsGetter(BenchmarkState s, Blackhole bh) {
        bh.consume(s.layout.dims());
    }

    @Benchmark
    public void stridesGetter(BenchmarkState s, Blackhole bh) {
        bh.consume(s.layout.strides());
    }

    // ---- view creation, which copies dims and strides to build the new layout ----

    @Benchmark
    public void narrowView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.narrow(0, true, 0, 2));
    }

    @Benchmark
    public void narrowAllView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.narrowAll(true, s.starts, s.ends));
    }

    @Benchmark
    public void selView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.sel(Order.C, 0, 1));
    }

    @Benchmark
    public void transposeView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.t());
    }

    @Benchmark
    public void permuteView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.permute(s.permutation));
    }

    @Benchmark
    public void moveAxisView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.moveAxis(0, s.rank - 1));
    }

    @Benchmark
    public void swapAxisView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.swapAxis(0, s.rank - 1));
    }

    @Benchmark
    public void expandView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.stretched.expand(0, 4));
    }

    @Benchmark
    public void squeezeView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.stretched.squeeze(0));
    }

    @Benchmark
    public void reshapeView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.reshape(s.flatShape));
    }

    @Benchmark
    public void unpadView(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.unpad(0, 0, 1));
    }

    // ---- the per-operation descriptors, which are the read only consumers of dims and strides ----

    @Benchmark
    public void densePointerIterator(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.ptrIterator(Order.C));
    }

    @Benchmark
    public void stridedPointerIterator(BenchmarkState s, Blackhole bh) {
        bh.consume(s.transposed.ptrIterator(Order.C));
    }

    @Benchmark
    public void strideLoopDescriptor(BenchmarkState s, Blackhole bh) {
        bh.consume(StrideLoopDescriptor.of(s.transposedLayout, Order.C, Simd.vsDouble));
    }

    @Benchmark
    public void tandemLoopDescriptor(BenchmarkState s, Blackhole bh) {
        bh.consume(TandemStrideLoopDescriptor.of(s.layout, s.layout, Order.C, Simd.vsDouble));
    }

    // ---- whole operations, where the bookkeeping competes with real work ----

    @Benchmark
    public void inPlaceScalarAdd(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.add_(1.0));
    }

    @Benchmark
    public void inPlaceArrayAdd(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.add_(s.y));
    }

    @Benchmark
    public void outOfPlaceArrayAdd(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.add(s.y));
    }

    @Benchmark
    public void sumReduction(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.sum());
    }

    @Benchmark
    public void sumOnShape(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.sumOn(s.reduceShape, false));
    }

    @Benchmark
    public void copyArray(BenchmarkState s, Blackhole bh) {
        bh.consume(s.x.copy());
    }

    @Benchmark
    public void catArrays(BenchmarkState s, Blackhole bh) {
        bh.consume(dm.cat(dt, Order.C, 0, List.of(s.x, s.y)));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(LayoutAccessBenchmark.class.getSimpleName())
                .resultFormat(ResultFormatType.CSV)
                .result(System.getProperty("resultPath", "layout-access.csv"))
                .warmupTime(TimeValue.seconds(1))
                .measurementTime(TimeValue.seconds(1))
                .build();
        new Runner(opt).run();
    }
}
