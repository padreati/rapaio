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
import rapaio.data.Frame;
import rapaio.data.preprocessing.RefSort;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.math.linear.base.DMatrixBase;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.sys.WS;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DMatrixDVectorDotBenchmark {

    @State(Scope.Benchmark)
    public static class VectorState {
        @Param( {"10", "100", "250", "500", "1000", "2500", "5000", "10000", "20000", "50000"})
        private int n;

        private final int m = 4000;

        private DMatrixDenseC mc;
        private DMatrixBase mb;
        private DVectorDense v;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            double[] dense = new double[n + 3];
            double[] mdense = new double[m * n];
            for (int i = 0; i < n; i++) {
                dense[i] = random.nextDouble();
                for (int j = 0; j < m; j++) {
                    mdense[i * m + j] = random.nextDouble();
                }
            }
            mb = new DMatrixBase(m, n, mdense);
            mc = new DMatrixDenseC(0, m, n, mdense);
            v = new DVectorDense(3, n, dense);
        }
    }

    @Benchmark
    public void testDotBase(VectorState s, Blackhole bh) {
        bh.consume(s.mb.dot(s.v));
    }

    @Benchmark
    public void testDotDenseC(VectorState s, Blackhole bh) {
        bh.consume(s.mc.dot(s.v));
    }

    public static void main(String[] args) throws RunnerException {
        runTest();
        printResults();
    }

    public static void runTest() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DMatrixDVectorDotBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(DMatrixDVectorDotBenchmark.class))
                .build();

        new Runner(opt).run();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(DMatrixDVectorDotBenchmark.class));
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
