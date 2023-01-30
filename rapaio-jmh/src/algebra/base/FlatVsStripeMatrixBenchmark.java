/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package algebra.base;

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
import jdk.incubator.vector.VectorSpecies;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class FlatVsStripeMatrixBenchmark {

    public static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    @State(Scope.Benchmark)
    public static class AllocationState {
        @Param( {"100", "1000", "2000"})
        int n;

        double[][] a;
        double[] b;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            a = new double[n][n];
            for (int i = 0; i < n; i++) {
                a[i] = new double[n];
            }
            b = new double[n * n];
        }
    }

    @Benchmark
    public void testAddStripeMatrix(AllocationState s, Blackhole sink) {
        for (int i = 0; i < s.n; i++) {
            int j = 0;
            int bound = SPECIES.loopBound(s.n);
            for (; j < bound; j += SPECIES.length()) {
                var v = DoubleVector.fromArray(SPECIES, s.a[i], j);
                v.add(1).intoArray(s.a[i], j);
            }
            for (; j < s.n; j++) {
                s.a[i][j] += 1;
            }
        }
        sink.consume(s.a);
    }

    @Benchmark
    public void testAddFlatMatrix(AllocationState s, Blackhole sink) {
        for (int i = 0; i < s.n * s.n; i += s.n) {
            int j = 0;
            int bound = SPECIES.loopBound(s.n);
            for (; j < bound; j += SPECIES.length()) {
                var v = DoubleVector.fromArray(SPECIES, s.b, i + j);
                v.add(1).intoArray(s.b, i + j);
            }
            for (; j < s.n; j++) {
                s.b[i + j] += 1;
            }
        }
        sink.consume(s.b);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FlatVsStripeMatrixBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(10)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(FlatVsStripeMatrixBenchmark.class))
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
