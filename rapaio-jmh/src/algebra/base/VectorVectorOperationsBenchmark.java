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

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

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
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/28/21.
 */
@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VectorVectorOperationsBenchmark {
    static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_MAX;

    @State(Scope.Benchmark)
    public static class VectorState {

        @Param( {"10000", "100000", "10000000"})
        int n;
        public double[] u;
        public double[] v;
        public double[] z;

        @Setup(Level.Invocation)
        public void setup() {
            RandomGenerator random = new Random(42);
            u = new double[n];
            v = new double[n];
            z = new double[n];
            for (int i = 0; i < n; i++) {
                u[i] = random.nextDouble();
                v[i] = random.nextDouble();
            }
        }
    }

    @Benchmark
    public void testAddArrays(VectorState s, Blackhole sink) {
        for (int i = 0; i < s.n; i++) {
            s.z[i] = s.u[i] + s.v[i];
        }
        sink.consume(s.z);
    }

    @Benchmark
    public void testAddVectorized(VectorState s, Blackhole sink) {
        int i = 0;
        int upperBound = SPECIES.loopBound(s.n);
        for (; i < upperBound; i += SPECIES.length()) {
            // FloatVector va, vb, vc;
            var va = DoubleVector.fromArray(SPECIES, s.u, i);
            var vb = DoubleVector.fromArray(SPECIES, s.v, i);
            va.add(vb).intoArray(s.z, i);
        }
        for (; i < s.n; i++) {
            s.z[i] = s.u[i] + s.v[i];
        }
        sink.consume(s.z);
    }

    @Benchmark
    public void testAddVectorizedInplace(VectorState s, Blackhole b) {
        int i = 0;
        s.z = new double[s.n];
        int bound = SPECIES.loopBound(s.n);
        for (; i < bound; i += SPECIES.length()) {
            var va = DoubleVector.fromArray(SPECIES, s.u, i);
            var vb = DoubleVector.fromArray(SPECIES, s.v, i);
            va.lanewise(VectorOperators.ADD, vb).intoArray(s.z, i);
        }
        for (; i < s.n; i++) {
            s.z[i] = s.u[i] + s.v[i];
        }
        b.consume(s.z);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(VectorVectorOperationsBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
