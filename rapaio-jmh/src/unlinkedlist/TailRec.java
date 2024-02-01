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

package unlinkedlist;

import java.lang.reflect.InvocationTargetException;
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
import rapaio.experiment.asm.CustomClassLoader;
import rapaio.experiment.asm.Factorial;
import rapaio.experiment.asm.FactorialDelegated;
import rapaio.experiment.asm.FactorialInterface;
import rapaio.experiment.asm.Sum;
import rapaio.experiment.asm.SumInterface;
import rapaio.util.collection.IntArrays;

@BenchmarkMode( {Mode.Throughput})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TailRec {

    @State(Scope.Benchmark)
    public static class MyState {
//        @Param( {"10", "100", "1000", "10000"})
        @Param({"1","3","5","10","15","20"})
        private int n;
        private int[] array;

        private FactorialInterface normalFactorial;
        private FactorialInterface optimizedFactorial;
        private SumInterface normalSum;
        private SumInterface optimizedSum;
        private FactorialDelegated factorialDelegated;

        @Setup(Level.Iteration)
        public void setup() throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
            array = IntArrays.newSeq(n);

            normalFactorial = new Factorial();
            optimizedFactorial = new CustomClassLoader(false).newTailRecInstance(Factorial.class);
            factorialDelegated = new FactorialDelegated();

            normalSum = new Sum();
            optimizedSum = new CustomClassLoader(false).newTailRecInstance(Sum.class);
        }
    }

//    @Benchmark
    public void recursiveSum(MyState s, Blackhole bh) {
        bh.consume(s.normalSum.sum(s.array));
    }

//    @Benchmark
    public void recursiveSumTailRec(MyState s, Blackhole bh) {
        bh.consume(s.optimizedSum.sum(s.array));
    }

    @Benchmark
    public void factorialRecursive(MyState s, Blackhole bh) {
        bh.consume(s.normalFactorial.fact(s.n));
    }

    @Benchmark
    public void recursiveRecursiveOptimized(MyState s, Blackhole bh) {
        bh.consume(s.optimizedFactorial.fact(s.n));
    }

    @Benchmark
    public void recursiveTailRecDelegate(MyState s, Blackhole bh) {
        bh.consume(s.factorialDelegated.fact(s.n));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(TailRec.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(5)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(TailRec.class))
                .build();

        new Runner(opt).run();
    }

}
