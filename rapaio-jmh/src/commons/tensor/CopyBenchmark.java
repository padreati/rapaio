/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package commons.tensor;

import static rapaio.graphics.opt.GOptions.color;
import static rapaio.graphics.opt.GOptions.labels;

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

import algebra.Utils;
import jsat.linear.DenseMatrix;
import rapaio.data.Frame;
import rapaio.data.preprocessing.RefSort;
import rapaio.data.preprocessing.VarApply;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.linear.dense.DMatrixDenseC;
import rapaio.linear.dense.DMatrixDenseR;
import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorMill;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CopyBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param( {"100","500", "1000", "2500", "5000", "10000"})
        private int n;

        private DMatrixDenseC mc;
        private DMatrixDenseR mr;

        private DenseMatrix jsatA;

        private DTensor tc;
        private DTensor tf;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            double[] array = DoubleArrays.newFrom(0, n * n, _ -> random.nextDouble());
            mc = new DMatrixDenseC(0, n, n, array);
            mr = new DMatrixDenseR(0, n, n, array);

            jsatA = new DenseMatrix(n, n);
            int p = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    jsatA.set(i, j, array[p++]);
                }
            }

            tc = TensorMill.array().stride(DType.DOUBLE, Shape.of(n, n), Order.C, array);
            tf = tc.copy(Order.F);
        }
    }

    @Benchmark
    public void testJSAT(BenchmarkState bs, Blackhole bh) {
        bh.consume(bs.jsatA.transpose());
    }

    @Benchmark
    public void tensorTransposeOrderC(BenchmarkState bs, Blackhole bh) {
        DTensor transpose = bs.tc.t();
        bh.consume(transpose);
    }

    @Benchmark
    public void tensorTransposeOrderF(BenchmarkState bs, Blackhole bh) {
        DTensor transpose = bs.tf.t();
        bh.consume(transpose);
    }

    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(CopyBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(CopyBenchmark.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(CopyBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(CopyBenchmark.class));
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
