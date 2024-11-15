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

package tensor;

import static rapaio.graphics.opt.GOpts.color;
import static rapaio.graphics.opt.GOpts.labels;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.DoubleBuffer;
import java.util.Arrays;
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
import jsat.linear.DenseVector;
import rapaio.data.Frame;
import rapaio.data.transform.RefSort;
import rapaio.data.transform.VarApply;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.Legend;
import rapaio.io.Csv;
import rapaio.math.narrays.Storage;
import rapaio.math.narrays.storage.array.DoubleArrayStorage;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class StorageBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        //        @Param( {"100", "1000", "10000"})
        @Param( {"10000"})
        private int n;

        private DenseVector jsatVector;

        private Storage<Double> doubleStorage;
        private DoubleBuffer doubleBuffer;
        private MemorySegment memorySegment;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random(42);
            double[] array = DoubleArrays.newFrom(0, n * n, _ -> random.nextDouble());

            jsatVector = new DenseVector(n);
            int p = 0;
            for (int i = 0; i < n; i++) {
                jsatVector.set(i, array[p++]);
            }

            doubleStorage = new DoubleArrayStorage(Arrays.copyOf(array, array.length));
            doubleBuffer = DoubleBuffer.wrap(Arrays.copyOf(array, array.length));
            memorySegment = MemorySegment.ofArray(Arrays.copyOf(array, array.length));
        }
    }

    @Benchmark
    public void absJSAT(BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.n; i++) {
            bs.jsatVector.set(i, Math.abs(bs.jsatVector.get(i)));
        }
        bh.consume(bs.jsatVector);
    }

    @Benchmark
    public void absDoubleStorage(BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.n; i++) {
            bs.doubleStorage.setDouble(i, Math.abs(bs.doubleStorage.getDouble(i)));
        }
        bh.consume(bs.doubleStorage);
    }

    @Benchmark
    public void absDoubleBuffer(BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.n; i++) {
            bs.doubleBuffer.put(i, Math.abs(bs.doubleBuffer.get(i)));
        }
        bh.consume(bs.doubleBuffer);
    }

    @Benchmark
    public void absDoubleMS(BenchmarkState bs, Blackhole bh) {
        for (int i = 0; i < bs.n; i++) {
            bs.memorySegment.setAtIndex(ValueLayout.JAVA_DOUBLE, i, Math.abs(bs.memorySegment.getAtIndex(ValueLayout.JAVA_DOUBLE, i)));
        }
        bh.consume(bs.memorySegment);
    }


    public static void main(String[] args) throws RunnerException, IOException {
        Options opt = new OptionsBuilder()
                .include(StorageBenchmark.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(2)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(3)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(StorageBenchmark.class))
                .build();
        new Runner(opt).run();
        Utils.resultPromote(StorageBenchmark.class);
        printResults();
    }

    public static void printResults() {
        Frame df = Csv.instance().quotes.set(true).read(Utils.resultPath(StorageBenchmark.class));
        Plot plot = Plotter.plot();
        int i = 1;
        for (String benchmark : df.rvar("Benchmark").levels().stream().skip(1).toList()) {
            Frame sel = df.stream().filter(s -> s.getLabel("Benchmark").equals(benchmark)).toMappedFrame();
            sel = sel.fapply(RefSort.by(sel.rvar("Score").refComparator()));
            plot.lines(sel.rvar("Param: n"), sel.rvar("Score").fapply(VarApply.onDouble(Math::log1p)), color(i));
            i++;
        }
        plot.legend(Legend.UP_LEFT, labels(df.rvar("Benchmark").levels().stream().skip(1).toArray(String[]::new)));
        WS.draw(plot);
    }
}
