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

package rapaio.tree;

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
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.ml.model.tree.CTree;

@BenchmarkMode( {Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CTreePerformance {


    @State(Scope.Benchmark)
    public static class VectorState {
        @Param( {"1000", "10000", "100000"})
        private int n;

        private Frame df;

        @Setup(Level.Invocation)
        public void setup() {
            Random random = new Random();
            VarDouble x1True = VarDouble.from(n, row -> Normal.of(10, 10).sampleNext()).name("x1");
            VarDouble x1False = VarDouble.from(n, row -> Normal.of(5, 10).sampleNext()).name("x1");

            String[] dict = new String[] {"a", "b", "c"};
            VarNominal x2True = VarNominal.from(n, row -> {
                int modIn = row % 3;
                int modOut = (modIn + 1) % 3;
                return dict[random.nextDouble() > 0.1 ? modIn : modOut];
            }, dict).name("x2");
            VarNominal x2False = VarNominal.from(n, row -> {
                int modIn = row % 3;
                int modOut = (modIn + 1) % 3;
                return dict[random.nextDouble() < 0.1 ? modIn : modOut];
            }, dict).name("x2");

            VarNominal yTrue = VarNominal.from(n, row -> "1", "1", "0").name("target");
            VarNominal yFalse = VarNominal.from(n, row -> "0", "1", "0").name("target");

            df = SolidFrame.byVars(x1True, x2True, yTrue)
                    .bindRows(SolidFrame.byVars(x1False, x2False, yFalse))
                    .copy();
        }
    }

    @Benchmark
    public void testCTree(VectorState s, Blackhole bh) {
        CTree cTree = CTree.newCART().maxDepth.set(-1);
        cTree.fit(s.df, "target");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CTreePerformance.class.getSimpleName())
                .warmupTime(TimeValue.seconds(2))
                .warmupIterations(3)
                .measurementTime(TimeValue.seconds(2))
                .measurementIterations(5)
                .forks(1)
                .resultFormat(ResultFormatType.CSV)
                .result(Utils.resultPath(CTreePerformance.class))
                .build();

        new Runner(opt).run();
    }
}
