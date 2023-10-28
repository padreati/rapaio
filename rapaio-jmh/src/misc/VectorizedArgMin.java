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

package misc;

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
import rapaio.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;

@BenchmarkMode( {Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VectorizedArgMin {

    @State(Scope.Benchmark)
    public static class VectorState {
        @Param( {"100000"})
        private int n;

        private double[] darray;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random();
            darray = DVector.random(random, n).array();
        }
    }

    @Benchmark
    public void testArray(VectorState s, Blackhole bh) {
        bh.consume(DoubleArrays.reverse(s.darray));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(misc.VectorizedArgMin.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(misc.VectorizedArgMin.class))
                .build();

        new Runner(opt).run();
    }

}



