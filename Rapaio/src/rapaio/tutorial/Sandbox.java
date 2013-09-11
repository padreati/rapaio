/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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
 */

package rapaio.tutorial;

import rapaio.core.BaseMath;
import rapaio.core.stat.Variance;
import rapaio.correlation.PearsonRCorrelation;
import rapaio.data.IndexVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.draw;
import rapaio.filters.NumericFilters;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sandbox {

    public static void main(String[] args) {
//        simulationFischerTea();
        simulation_random_noise();
    }

    private static void simulation_random_noise() {
        final int LEN = 100_000_000;

//        Uniform unif = new Uniform(0, 1);

        Vector x = new NumericVector("x", LEN);
        Vector y = new NumericVector("y", LEN);

        for (int i = 0; i < LEN; i++) {
            x.setValue(i, i + 1);
            y.setValue(i, i + 1);
        }

        Variance v = new Variance(y);
        y = NumericFilters.jitter(y, BaseMath.sqrt(v.getValue()));

        Summary.summary(new PearsonRCorrelation(x, y));

//        Plot p = new Plot();
//        p.add(new Points(p, x, y));
//        draw(p);
    }

    public static void simulationFischerTea() {

        List<Integer> tea = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            tea.add(i);
        }

        final int TIMES = 100_000;
        int success = 0;
        Vector freq = new NumericVector("freq", TIMES);
        for (int i = 0; i < TIMES; i++) {
            Collections.shuffle(tea);
            boolean valid = true;
            for (int j = 0; j < 4; j++) {
                if (tea.get(j) > 4) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                success++;
            }
            freq.setValue(i, success / (1. * i));
        }

        Plot p = new Plot();
        p.add(new Lines(p, new IndexVector("x", 0, TIMES - 1, 1), freq));
        draw(p);

        System.out.println(1. / 70.);
    }
}
