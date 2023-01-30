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

import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
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
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.experiment.math.tensor.storage.DStorage;
import rapaio.math.linear.DVector;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class VectorizedArgMin {

    @State(Scope.Benchmark)
    public static class VectorState {
        @Param( {"100000"})
        private int n;

        private double[] darray;
        private DStorage dstorage;
        private MemorySegment segment;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random();
            darray = DVector.random(random, n).array();
            dstorage = DStorage.random(n, random);
            segment = MemorySegment.allocateNative(8L * n, MemorySession.global());
        }
    }

    @Benchmark
    public void testArray(VectorState s, Blackhole bh) {
        bh.consume(DVector.wrap(s.darray).min());
    }

    @Benchmark
    public void testVectorizedArray(VectorState s, Blackhole bh) {
        bh.consume(s.dstorage.min(0, s.n));
    }

    @Benchmark
    public void testVectorizedMemorySegment(VectorState s, Blackhole bh) {
        double m = Segment.min(s.segment, 0, s.n);
        bh.consume(m);
    }

    static class Segment {

        private static final VectorSpecies<Double> SPECIES = DoubleVector.SPECIES_PREFERRED;
        private static final int SPECIES_LEN = SPECIES.length();
        private static final ByteOrder BYTE_ORDER = ByteOrder.nativeOrder();
        private static final ValueLayout.OfDouble LAYOUT = ValueLayout.JAVA_DOUBLE;

        public static double min(MemorySegment segment, int start, int len) {
            if (len <= 0) {
                return Float.NaN;
            }
            int loopBound = SPECIES.loopBound(len) + start;
            int i = start;
            boolean vectorized = false;
            DoubleVector minVector = DoubleVector.broadcast(SPECIES, Float.POSITIVE_INFINITY);
            for (; i < loopBound; i += SPECIES_LEN) {
                DoubleVector xv = DoubleVector.fromMemorySegment(SPECIES, segment, i, BYTE_ORDER);
                minVector = minVector.min(xv);
                vectorized = true;
            }
            double minValue = vectorized ? minVector.reduceLanes(VectorOperators.MIN) : Double.NaN;
            for (; i < start + len; i++) {
                minValue = Double.isNaN(minValue) ? segment.getAtIndex(LAYOUT, i) : Math.min(minValue, segment.getAtIndex(LAYOUT, i));
            }
            return minValue;
        }
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



