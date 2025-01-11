/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package unlinkedlist;

import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.labels;

import java.io.IOException;
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

import commons.Utils;
import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Simd;
import rapaio.data.Frame;
import rapaio.data.transform.RefSort;
import rapaio.data.transform.VarApply;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.sys.WS;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class Megamorphic {

    private static final Random random = new Random(42);
    private static final DArrayManager dm = DArrayManager.base();
    private static final DType<?> dt = DType.FLOAT;
    private static final VectorSpecies<Float> vs = FloatVector.SPECIES_PREFERRED;

    @State(Scope.Benchmark)
    public static class BenchmarkState {

        @Param( {"10", "100", "1000"})
        private int n;
        private float[] m;

        @Setup(Level.Invocation)
        public void setup() {
            m = new float[n * n];
            for (int i = 0; i < n * n; i++) {
                m[i] = random.nextFloat();
            }
        }

    }

    public interface Operation {
        default double compute(float[] m) {
            return comp(m);
        }

        double comp(float[] m);
    }

    public static class OperationA1 implements Operation {
        @Override
        public double comp(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationA2 implements Operation {
        @Override
        public double comp(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationA3 implements Operation {
        @Override
        public double comp(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationA4 implements Operation {
        @Override
        public double comp(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationB1 implements Operation {
        @Override
        public double comp(float[] m) {
            return _compute(m);
        }

        public double _compute(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationB2 implements Operation {
        @Override
        public double comp(float[] m) {
            return _compute(m);
        }

        public double _compute(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationB3 implements Operation {
        @Override
        public double comp(float[] m) {
            return _compute(m);
        }

        public double _compute(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    public static class OperationB4 implements Operation {
        @Override
        public double comp(float[] m) {
            return _compute(m);
        }

        public double _compute(float[] m) {
            float result = 0;
            FloatVector a = Simd.zeroFloat();
            int i = 0;
            for (; i < vs.loopBound(m.length); i += vs.length()) {
                FloatVector v = FloatVector.fromArray(vs, m, i);
                a = a.add(v);
            }
            result += a.reduceLanes(VectorOperators.ADD);
            for (; i < m.length; i++) {
                result += m[i];
            }
            return result;
        }
    }

    private static final int T = 10_000;

    @Benchmark
    public void callNormal(Megamorphic.BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < T; i++) {
            Operation op = switch (random.nextInt(4)) {
                case 0 -> new OperationA1();
                case 1 -> new OperationA2();
                case 2 -> new OperationA3();
                case 3 -> new OperationA4();
                default -> null;
            };
            bh.consume(op.compute(bs.m));
        }
    }

    @Benchmark
    public void callPrivate(Megamorphic.BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < T; i++) {
            Operation op = switch (random.nextInt(4)) {
                case 0 -> new OperationB1();
                case 1 -> new OperationB2();
                case 2 -> new OperationB3();
                case 3 -> new OperationB4();
                default -> null;
            };
            bh.consume(op.compute(bs.m));
        }
    }


    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(Megamorphic.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(Megamorphic.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(Megamorphic.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(Megamorphic.class));
        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark)).toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(sel.rvar("Param: n"), sel.rvar("Score").fapply(VarApply.onDouble(Math::log1p)), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().stream().skip(1).toArray(String[]::new)));
        WS.draw(plot);
    }
}
