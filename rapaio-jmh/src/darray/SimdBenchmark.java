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

package darray;

import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.labels;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import commons.Utils;
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Simd;
import rapaio.darray.Storage;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.operator.DArrayOp;
import rapaio.data.Frame;
import rapaio.data.transform.RefSort;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.sys.WS;

@BenchmarkMode( {Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SimdBenchmark {

    private static final DArrayManager base = DArrayManager.base();
    private static final DType<?> dt = DType.DOUBLE;

    @State(Scope.Benchmark)
    public static class SimdBenchmarkState {
        @Param( {"15", "105", "1325"})
//        @Param( {"15", "105", "1325", "13647", "152437"})
        private int n;

        double[] array;
        Storage storage;
        StrideLoopDescriptor<Double> loop;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            array = new double[n];
            for (int i = 0; i < array.length; i++) {
                array[i] = random.nextDouble();
            }
            storage = base.storageManager().from(dt, array);
            loop = StrideLoopDescriptor.of(
                    StrideLayout.of(new int[] {n}, 0, new int[] {1}), Order.C, Simd.vsDouble);

        }
    }

//    @Benchmark
    public void sqrArray(SimdBenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.array.length; i++) {
            double value = bs.array[i];
            bs.array[i] = value * value;
        }
        bh.consume(bs.storage);
    }

//    @Benchmark
    public void sqrStorageSingle(SimdBenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.storage.size(); i++) {
            double value = bs.storage.getDouble(i);
            bs.storage.setDouble(i, value * value);
        }
        bh.consume(bs.storage);
    }

//    @Benchmark
    public void sqrStorageVector(SimdBenchmarkState bs, Blackhole bh) {
        VectorSpecies<Double> vs = Simd.vsDouble;

        int i = 0;
        for (; i < vs.loopBound(bs.storage.size()); i += vs.length()) {
            DoubleVector a = bs.storage.getDoubleVector(i);
            a = a.mul(a);
            bs.storage.setDoubleVector(a, i);
        }
        for (; i < bs.storage.size(); i++) {
            double value = bs.storage.getDouble(i);
            bs.storage.setDouble(i, value * value);
        }
        bh.consume(bs.storage);
    }

    @Benchmark
    public void sqrOpWithLoop(SimdBenchmarkState bs, Blackhole bh) {
        StrideLoopDescriptor<Double> loop = StrideLoopDescriptor.of(
                StrideLayout.of(new int[] {bs.storage.size()}, 0, new int[] {1}), Order.C, Simd.vsDouble);
        DArrayOp.unarySqr().applyDouble(loop, bs.storage);
        bh.consume(bs.storage);
    }

    @Benchmark
    public void sqrOpWithoutLoop(SimdBenchmarkState bs, Blackhole bh) {
        DArrayOp.unarySqr().applyDouble(bs.loop, bs.storage);
        bh.consume(bs.storage);
    }

    public static void main() throws RunnerException, IOException {

        Options opt = new OptionsBuilder()
                .include(SimdBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(SimdBenchmark.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(SimdBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(SimdBenchmark.class));
        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark)).toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(sel.rvar("Param: n"), sel.rvar("Score"), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().toArray(String[]::new)));
        plot.yLim(0, Double.NaN);
        WS.draw(plot);
    }
}
