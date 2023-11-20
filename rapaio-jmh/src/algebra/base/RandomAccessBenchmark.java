/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
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
import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.AverageTime)
public class RandomAccessBenchmark {


    private static final VectorSpecies<Double> SPEC = DoubleVector.SPECIES_PREFERRED;
    private static final int SPEC_LEN = SPEC.length();

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"10000","100000", "1000000"})
        int n;

        double[] a;
        double[] b;
        double[] c;
        int[] indexes1;
        int[] indexes2;

        @Setup(Level.Invocation)
        public void setupInvocation() {
            Random random = new Random(42);
            a = DoubleArrays.newFrom(0, n, _ -> random.nextDouble());
            b = DoubleArrays.newFrom(0, n, _ -> random.nextDouble());
            c = new double[n];
            indexes1 = IntArrays.newSeq(0, n);
            indexes2 = IntArrays.newSeq(0, n);
            IntArrays.shuffle(indexes1, random);
            IntArrays.shuffle(indexes2, random);
        }
    }

    @Benchmark
    public void testBase(MyState s, Blackhole bh) {
        for (int i = 0; i < s.n; i++) {
            s.c[i] = s.a[s.indexes1[i]] + s.b[s.indexes2[i]];
        }
        bh.consume(s.c);
    }

    @Benchmark
    public void testVectorized(MyState s, Blackhole bh) {
        int loopBound = SPEC.loopBound(s.n);
        int i = 0;
        for (; i < loopBound; i += SPEC_LEN) {
            var va = DoubleVector.fromArray(SPEC, s.a, 0, s.indexes1, i);
            var vb = DoubleVector.fromArray(SPEC, s.b, 0, s.indexes2, i);
            var vc = va.add(vb);
            vc.intoArray(s.c, i);
        }
        for (; i < s.n; i++) {
            s.c[i] = s.a[s.indexes1[i]] + s.b[s.indexes2[i]];
        }
        bh.consume(s.c);
    }

    private double[] newPermutation(double[] array, int[] perm) {
        double[] copy = new double[perm.length];
        int i = 0;
        int loopBound = SPEC.loopBound(copy.length);
        for (; i < loopBound; i += SPEC_LEN) {
            DoubleVector.fromArray(SPEC, array, 0, perm, i)
                    .intoArray(copy, i);
        }
        for (; i < copy.length; i++) {
            copy[i] = array[perm[i]];
        }
        return copy;
    }

    @Benchmark
    public void testVectorizedBuffer(MyState s, Blackhole bh) {
        double[] a = newPermutation(s.a, s.indexes1);
        double[] b = newPermutation(s.b, s.indexes2);

        int loopBound = SPEC.loopBound(s.n);
        int i = 0;
        for (; i < loopBound; i += SPEC_LEN) {
            var va = DoubleVector.fromArray(SPEC, a, i);
            var vb = DoubleVector.fromArray(SPEC, b, i);
            var vc = va.add(vb);
            vc.intoArray(s.c, i);
        }
        for (; i < s.n; i++) {
            s.c[i] = s.a[i] + b[i];
        }
        bh.consume(s.c);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RandomAccessBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(RandomAccessBenchmark.class))
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
