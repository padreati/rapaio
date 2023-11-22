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
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorSpecies;
import rapaio.data.Frame;
import rapaio.data.preprocessing.RefSort;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.linear.dense.DVectorDense;
import rapaio.sys.WS;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class DVectorAddVectorToVectorBenchmark {

    @State(Scope.Benchmark)
    public static class VectorState {
        //        @Param( {"100", "1000", "10000", "100000", "1000000", "10000000", "100000000", "200000000"})
        @Param( {"1000", "1000000"})
        private int n;

        private double[] arraya;
        private double[] arrayb;
        private double[] arrayc;
        private DVectorDense va;
        private DVectorDense vb;
        private DVectorDense vc;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            arraya = new double[n + 3];
            arrayb = new double[n + 4];
            arrayc = new double[n + 5];
            for (int i = 0; i < n; i++) {
                arraya[3 + i] = random.nextDouble();
                arrayb[4 + i] = random.nextDouble();
            }
            va = new DVectorDense(3, n, arraya);
            vb = new DVectorDense(4, n, arrayb);
            vc = new DVectorDense(5, n, arrayc);
        }
    }

    @Benchmark
    public void testDense(VectorState s, Blackhole bh) {
        s.va.addTo(s.vc, s.vb);
        bh.consume(s.vc);
    }

    private static final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;

    @Benchmark
    public void testVArray(VectorState s, Blackhole bh) {
        int loopBound = species.loopBound(s.n);
        int i = 0;
        for (; i < loopBound; i += species.length()) {
            var va = DoubleVector.fromArray(species, s.arraya, 3 + i);
            var vb = DoubleVector.fromArray(species, s.arrayb, 4 + i);
            va.add(vb).intoArray(s.arrayc, 5 + i);
        }
        VectorMask<Double> m = species.indexInRange(i, s.n);
        var va = DoubleVector.fromArray(species, s.arraya, 3 + i, m);
        var vb = DoubleVector.fromArray(species, s.arrayb, 4 + i, m);
        va.add(vb).intoArray(s.arrayc, 5 + i, m);
        bh.consume(s.vc);
    }

    public static void main(String[] args) throws RunnerException {
        runTest();
        printResults();
    }

    public static void runTest() throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DVectorAddVectorToVectorBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(DVectorAddVectorToVectorBenchmark.class))
//                .jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions", "-XX:CompilerDirectivesFile=/home/ati/work/rapaio-jmh/src/compiler.json")
//                .jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining", "-XX:CompilerDirectivesFile=/home/ati/work/rapaio-jmh/src/compiler.json")
//                .jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintCompilation")
//                .jvmArgsAppend("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining")
                .build();

        new Runner(opt).run();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(DVectorAddVectorToVectorBenchmark.class));

        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels().stream().skip(1).toList()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark))
                    .toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(/*sel.rvar("Param: n"),*/ sel.rvar("Score").dv().applyNew(Math::log1p).dv(), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().stream().skip(1).toArray(String[]::new)));

        WS.draw(plot);
    }
}
