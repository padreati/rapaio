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

import java.util.ArrayList;
import java.util.Iterator;
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
import rapaio.util.collection.IntArrays;

@BenchmarkMode( {Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ForEachOrNot {

    @State(Scope.Benchmark)
    public static class MyState {
        @Param( {"100", "1000", "10000", "100000", "1000000"})
        private int len;
        private int[] array;
        private ArrayList<Integer> arrayList;

        @Setup(Level.Invocation)
        public void setup() {
            array = IntArrays.newSeq(0, len);
            arrayList = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                arrayList.add(i);
            }
        }
    }

    @Benchmark
    public void arrayFori(MyState s, Blackhole bh) {
        long sum = 0;
        for (int i = 0; i < s.len; i++) {
            sum += s.array[i];
        }
        bh.consume(sum);
    }

    @Benchmark
    public void arrayForeach(MyState s, Blackhole bh) {
        long sum = 0;
        for (int i : s.array) {
            sum += i;
        }
        bh.consume(sum);
    }

    @Benchmark
    public void arrayListFori(MyState s, Blackhole bh) {
        long sum = 0;
        Iterator<Integer> it = s.arrayList.iterator();
        while (it.hasNext()) {
            sum += it.next();
        }
        bh.consume(sum);
    }

    @Benchmark
    public void arrayListForeach(MyState s, Blackhole bh) {
        long sum = 0;
        for (int i : s.arrayList) {
            sum += i;
        }
        bh.consume(sum);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ForEachOrNot.class.getSimpleName())
                .warmupTime(TimeValue.seconds(1))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(1))
                .measurementIterations(10)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(ForEachOrNot.class))
                .build();

        new Runner(opt).run();
    }
}

