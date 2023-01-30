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
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

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
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.dense.DMatrixDenseR;
import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/12/21.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MatrixMatrixMultBenchmark {

    private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"100", "1000", "2000"})
        public int n;
        public double[] a;
        public double[] b;

        @Setup(Level.Invocation)
        public void setUpInvocation() {
            RandomGenerator random = new Random();
            a = DoubleArrays.newFrom(0, n * n, row -> random.nextDouble());
            b = DoubleArrays.newFrom(0, n * n, row -> random.nextDouble());
        }
    }

    @Benchmark
    public void testMatrixMatrixProd(MyState s, Blackhole bh) {
        DMatrix c = DMatrixDenseR.wrap(s.n, s.n, s.a).dot(DMatrixDenseR.wrap(s.n, s.n, s.b));
        bh.consume(c);
    }

    @Benchmark
    public void testMatrixMatrixProdVectorized(MyState s, Blackhole bh) {
        int ilen = s.n;
        int jlen = s.n;
        int klen = s.n;
        double[][] c = new double[ilen][jlen];
        for (int i = 0; i < ilen; i++) {
            c[i] = new double[jlen];
        }
        int bound = SPECIES.loopBound(klen);
        IntStream.range(0, jlen / 16).parallel().forEach(ss -> {
            double[] bv = new double[s.b.length];
            for (int j = ss; j < Math.min(ss + 16, jlen); j++) {
                System.arraycopy(s.b, j * s.n, bv, 0, s.b.length);
                for (int i = 0; i < ilen; i++) {
                    int k = 0;
                    var vc = DoubleVector.zero(SPECIES);
                    for (; k < bound; k += SPECIES.length()) {
                        var va = DoubleVector.fromArray(SPECIES, s.a, k + i * s.n);
                        var vb = DoubleVector.fromArray(SPECIES, bv, k);
                        vc = va.fma(vb, vc);
                    }
                    double value = vc.reduceLanes(VectorOperators.ADD);
                    for (; k < s.b.length; k++) {
                        value += s.a[k + i * s.n] * bv[k];
                    }
                    c[i][j] = value;
                }
            }
        });
        bh.consume(c);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MatrixMatrixMultBenchmark.class.getSimpleName())
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(5)
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
