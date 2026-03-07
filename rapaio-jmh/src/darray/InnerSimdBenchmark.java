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
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.darray.iterators.StrideLoopDescriptor;
import rapaio.darray.layout.StrideLayout;
import rapaio.data.Frame;
import rapaio.data.transform.RefSort;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.sys.WS;

@BenchmarkMode( {Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class InnerSimdBenchmark {

    private static final DArrayManager base = DArrayManager.base();
    private static final DType<?> dt = DType.DOUBLE;

    @State(Scope.Benchmark)
    public static class SimdBenchmarkState {
//        @Param( {"10", "100", "1000", "10000"})
        @Param( {"100", "1000"})
        private int n;
        @Param( {"1", "2", "10", "173"})
        private int stride;

        double[] arrayA;
        double[] arrayB;
        DArray<?> a;
        DArray<?> b;
        StrideLoopDescriptor loop;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            arrayA = new double[n];
            arrayB = new double[n * stride];
            for (int i = 0; i < arrayA.length; i++) {
                arrayA[i] = random.nextDouble();
                arrayB[i * stride] = random.nextDouble();
            }
            a = base.stride(dt, StrideLayout.of(Shape.of(n), 0, new int[] {1}), arrayA);
            b = base.stride(dt, StrideLayout.of(Shape.of(n), 0, new int[] {stride}), arrayB);
        }
    }

    @Benchmark
    public void innerWithoutCopy(SimdBenchmarkState bs, Blackhole bh) {
        bh.consume(bs.a.inner(bs.b));
    }

    @Benchmark
    public void innerWithCopy(SimdBenchmarkState bs, Blackhole bh) {
        DArray<?> bb = bs.b.copy();
        bh.consume(bs.a.inner(bb));
    }

    static void main() throws RunnerException, IOException {

        Options opt = new OptionsBuilder()
                .include(InnerSimdBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(InnerSimdBenchmark.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(InnerSimdBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(InnerSimdBenchmark.class));
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
