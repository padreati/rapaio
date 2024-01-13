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

package algebra.rapaio;

import static rapaio.graphics.opt.GOptions.color;
import static rapaio.graphics.opt.GOptions.labels;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.math.linear.base.DMatrixBase;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DMatrixDenseR;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorEngine;
import rapaio.sys.WS;


@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DMatrixDMatrixDotBenchmark {

    @State(Scope.Benchmark)
    public static class StateDMDM {
        //                @Param( {"100","250","500","1000","1500","2000","3000","4000"})
        @Param( {"100", "250", "500", "1000"})
        private int n;

        private DMatrixBase mb;
        private DMatrixDenseC mc;
        private DMatrixDenseR mr;

        private DenseMatrix jsatDM;

        private Tensor<Double> t;
        private Tensor<Double> tc;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            double[] array = new double[n * n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    array[i * n + j] = random.nextDouble();
                }
            }
            mb = new DMatrixBase(n, n, array);
            mc = new DMatrixDenseC(0, n, n, array);
            mr = new DMatrixDenseR(0, n, n, array);

            jsatDM = new DenseMatrix(n, n);
            int p = 0;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    jsatDM.set(i, j, array[p++]);
                }
            }

            t = TensorEngine.varray().ofDouble().stride(Shape.of(n, n), Order.C, array);
            tc = t.transpose().copy(Order.F);
        }
    }

    @Benchmark
    public void testDotBaseBase(StateDMDM s, Blackhole bh) {
        bh.consume(s.mb.dot(s.mb));
    }

//    @Benchmark
//    public void testDotCC(StateDMDM s, Blackhole bh) {
//        bh.consume(s.mc.dot(s.mc));
//    }
//
//    @Benchmark
//    public void testDotCR(StateDMDM s, Blackhole bh) {
//        bh.consume(s.mc.dot(s.mr));
//    }
//
//    @Benchmark
//    public void testDotRR(StateDMDM s, Blackhole bh) {
//        bh.consume(s.mr.dot(s.mr));
//    }
//
//    @Benchmark
//    public void testDotRC(StateDMDM s, Blackhole bh) {
//        bh.consume(s.mr.dot(s.mc));
//    }

    @Benchmark
    public void testJSAT(StateDMDM s, Blackhole bh) throws InterruptedException {
        ExecutorService service = Executors.newCachedThreadPool();
        bh.consume(s.jsatDM.multiply(s.jsatDM, service));
        service.shutdownNow();
        service.awaitTermination(1L, TimeUnit.MILLISECONDS);
    }

    @Benchmark
    public void testTensorRC(StateDMDM s, Blackhole bh) {
        bh.consume(s.t.mm(s.tc));
    }

    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(DMatrixDMatrixDotBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(DMatrixDMatrixDotBenchmark.class))
                .build();

        new Runner(opt).run();
        Utils.resultPromote(DMatrixDMatrixDotBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(DMatrixDMatrixDotBenchmark.class));
        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels().stream().skip(1).toList()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark))
                    .toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(sel.rvar("Param: n"), sel.rvar("Score"), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().stream().skip(1).toArray(String[]::new)));
        WS.draw(plot);
    }
}
