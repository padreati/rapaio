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
import jsat.linear.DenseMatrix;
import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.transform.RefSort;
import rapaio.data.transform.VarApply;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class AbsBenchmark {

    private static final DArrayManager base = DArrayManager.base();
    private static final DType<?> dt = DType.DOUBLE;

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param( {"100","1000","10000"})
        private int n;

        private DenseMatrix jsatA;

        private DArray<?> bTc;
        private DArray<?> bTf;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            double[] array = DoubleArrays.newFrom(0, n * n, _ -> random.nextDouble());

            jsatA = new DenseMatrix(n, n);
            int p = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    jsatA.set(i, j, array[p++]);
                }
            }

            bTc = base.stride(dt, Shape.of(n, n), Order.C, array);
            bTf = base.stride(dt, Shape.of(n, n), Order.F, array);
        }
    }

    @Benchmark
    public void fillJSAT(BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.jsatA.rows(); i++) {
            for (int j = 0; j < bs.jsatA.cols(); j++) {
                bs.jsatA.set(i, j, Math.abs(bs.jsatA.get(i, j)));
            }
        }
        bh.consume(bs.jsatA);
    }

    @Benchmark
    public void fillBaseOrderC(BenchmarkState bs, Blackhole bh) {
        bh.consume(bs.bTc.abs_());
    }

    @Benchmark
    public void fillBaseOrderF(BenchmarkState bs, Blackhole bh) {
        bh.consume(bs.bTf.abs_());
    }


    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(AbsBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(AbsBenchmark.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(AbsBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(AbsBenchmark.class));
        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels().stream().skip(1).toList()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark)).toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(sel.rvar("Param: n"), sel.rvar("Score").fapply(VarApply.onDouble(Math::log1p)), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().stream().skip(1).toArray(String[]::new)));
        WS.draw(plot);
    }
}
