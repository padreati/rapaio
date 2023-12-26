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

package rapaio.experiment.math.regression;

import rapaio.data.Frame;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.ml.model.linear.BinaryLogistic;
import rapaio.printer.opt.POpts;

/**
 * Inspired by <a href="http://tullo.ch/articles/speeding-up-isotonic-regression/"></a>.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/5/16.
 */
public class IsotonicRegression {

    public DVector isotonic_regression(DVector y, DVector weight) {

        int n = y.size();

        // The algorithm proceeds by iteratively updating the solution array.

        DVectorDense solution = DVectorDense.copy(y);

        if (n <= 1) {
            return solution;
        }

        n -= 1;
        boolean pooled = true;
        while (pooled) {
            // repeat until there are no more adjacent violators.
            int i = 0;
            pooled = false;
            while (i < n) {
                int k = i;
                while (k < n && solution.get(k) >= solution.get(k + 1)) {
                    k += 1;
                }
                if (solution.get(i) != solution.get(k)) {
                    // solution[i:k + 1] is a decreasing subsequence, so
                    // replace each point in the subsequence with the
                    // weighted average of the subsequence.
                    double numerator = 0.0;
                    double denominator = 0.0;
                    for (int j = i; j < k + 1; j++) {
                        numerator += solution.get(j) * weight.get(j);
                        denominator += weight.get(j);
                    }
                    for (int j = i; j < k + 1; j++) {
                        solution.set(j, numerator / denominator);
                    }
                    pooled = true;
                }
                i = k + 1;
            }
        }
        return solution;
    }

    public static void main(String[] args) {

        Frame iris = Datasets.loadIrisDataset()
                .stream().filter(s -> !s.getLabel("class").equals("virginica")).toMappedFrame();
        VarNominal clazz = VarNominal.from(iris.rowCount(), row -> iris.rvar("class").getLabel(row)).name("clazz");
        Frame df = iris.removeVars("class").bindVars(clazz).copy();


        df.printSummary(POpts.textWidth(-1));

        df = df.mapVars("sepal-length","clazz");

        BinaryLogistic lr = BinaryLogistic.newModel();
        lr.fit(df, "clazz").printFullContent();

        lr.predict(df, true, true).printSummary();
    }
}
