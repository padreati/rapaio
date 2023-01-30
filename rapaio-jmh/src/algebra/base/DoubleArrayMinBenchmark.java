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

package algebra.base;

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
import rapaio.util.collection.DoubleArrays;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class DoubleArrayMinBenchmark {

    @State(Scope.Benchmark)
    public static class AllocationState {
        @Param({"100", "1000", "10000", "100000", "1000000", "10000000"})
        int n;

        double[] v;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            v = DoubleArrays.newFrom(0, n, row -> new Random().nextDouble());
        }
    }

    @Benchmark
    public void testMinVectorized(AllocationState s, Blackhole sink) {
        double min = DoubleArrays.min(s.v, 2, s.n - 2);
        sink.consume(min);
    }

    public static double minSimple(double[] values, int offset, int size) {
        double min = values[offset];
        for (int i = offset + 1; i < offset + size; i++) {
            if (min > values[i]) {
                min = values[i];
            }
        }
        return min;
    }

    @Benchmark
    public void testMinSimple(AllocationState s, Blackhole sink) {
        double min = minSimple(s.v, 2, s.n - 2);
        sink.consume(min);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DoubleArrayMinBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(10)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(DoubleArrayMinBenchmark.class))
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
