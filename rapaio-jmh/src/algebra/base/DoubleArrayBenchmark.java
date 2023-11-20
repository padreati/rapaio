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
import rapaio.util.collection.IntArrays;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class DoubleArrayBenchmark {

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"100000", "1000000"})
        int n;

        double[] v;
        int[] indexes;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            v = DoubleArrays.newFrom(0, n, row -> new Random().nextDouble());
            indexes = IntArrays.newSeq(0, n);
            IntArrays.shuffle(indexes, new Random(42));
        }
    }

    @Benchmark
    public void testContiguous(MyState s, Blackhole sink) {
        for (int i = 0; i < s.n; i++) {
            s.v[i] += (i & 1) + 1;
        }
        sink.consume(s.v);
    }

    @Benchmark
    public void testReversed(MyState s, Blackhole sink) {
        for (int i = s.n - 1; i >= 0; i--) {
            s.v[i] += (i & 1) + 1;
        }
        sink.consume(s.v);
    }

    @Benchmark
    public void testIndexed(MyState s, Blackhole bh) {
        for (int i : s.indexes) {
            s.v[i] += (i & 1) + 1;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DoubleArrayBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(DoubleArrayBenchmark.class))
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
