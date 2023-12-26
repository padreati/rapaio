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
import rapaio.math.linear.dense.DVectorDense;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/1/21.
 */
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MatrixVectorProdBenchmark {

    public static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"10", "100", "1000"})
        public int rows;
        @Param( {"10", "100", "1000", "10000", "100000"})
        public int cols;

        double[][] stripe;
        double[] flat;

        double[] vector;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            stripe = new double[rows][cols];
            flat = new double[rows * cols];

            Random random = new Random(42);
            int pos = 0;
            for (int i = 0; i < rows; i++) {
                stripe[i] = new double[cols];
                for (int j = 0; j < cols; j++) {
                    double value = random.nextDouble();
                    stripe[i][j] = value;
                    flat[pos++] = value;
                }
            }

            vector = new double[cols];
            for (int i = 0; i < cols; i++) {
                vector[i] = random.nextDouble();
            }
        }
    }

    @Benchmark
    public void testArrayParallel(MyState s, Blackhole bh) {
        double[] c = new double[s.rows];
        IntStream.range(0, s.rows).parallel().forEach(i -> c[i] = prodSum(s.stripe[i], s.vector));
        bh.consume(new DVectorDense(0, c.length, c));
    }

    @Benchmark
    public void testArraySingle(MyState s, Blackhole bh) {
        double[] c = new double[s.rows];
        for (int i = 0; i < s.rows; i++) {
            c[i] = prodSum(s.stripe[i], s.vector);
        }
        bh.consume(new DVectorDense(0, c.length, c));
    }

    private double prodSum(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    @Benchmark
    public void testVectorizedParallel(MyState s, Blackhole bh) {
        double[] c = new double[s.rows];
        IntStream.range(0, s.rows).parallel()
                .forEach(i -> c[i] = dotSum(s.flat, i * s.cols, s.vector, s.cols));
        bh.consume(new DVectorDense(0, c.length, c));
    }

    @Benchmark
    public void testVectorizedSingle(MyState s, Blackhole bh) {
        double[] c = new double[s.rows];
        for (int i = 0; i < s.rows; i++) {
            c[i] = dotSum(s.flat, i * s.cols, s.vector, s.cols);
        }
        bh.consume(new DVectorDense(0, c.length, c));
    }

    public static double dotSum(double[] x, int xOff, double[] y, int length) {
        int loopBound = SPECIES.loopBound(length) + xOff;
        int i = xOff;
        var vsum = DoubleVector.zero(SPECIES);
        for (; i < loopBound; i += SPECIES.length()) {
            var vx = DoubleVector.fromArray(SPECIES, x, i);
            var vy = DoubleVector.fromArray(SPECIES, y, i - xOff);
            vsum = vx.fma(vy, vsum);
        }
        double sum = vsum.reduceLanes(VectorOperators.ADD);
        int xLen = length + xOff;
        for (; i < xLen; i++) {
            sum += x[i] * y[i - xOff];
        }
        return sum;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MatrixVectorProdBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(10)
                .forks(1)
                .build();

        new Runner(opt).run();

    }
}
