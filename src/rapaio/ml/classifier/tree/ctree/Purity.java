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

package rapaio.ml.classifier.tree.ctree;

import rapaio.core.tests.ChiSqIndependence;
import rapaio.core.tools.DensityTable;
import rapaio.util.collection.DoubleArrayTools;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public enum Purity implements Serializable {

    InfoGain {
        @Override
        public double compute(DensityTable<String, String> dt) {
            return dt.splitByRowInfoGain();
        }
    },
    GainRatio {
        @Override
        public double compute(DensityTable<String, String> dt) {
            return dt.splitByRowGainRatio();
        }
    },
    GiniGain {
        @Override
        public double compute(DensityTable<String, String> dt) {

            double[] rowTotals = dt.rowTotals();
            double[] colTotals = dt.colTotals();
            double total = DoubleArrayTools.nansum(rowTotals, 0, rowTotals.length);
            if (total <= 0) {
                // no instances | all missing on test
                return 0;
            }

            // compute before split gini impurity
            double gini = 1.0;
            for (int i = 0; i < dt.colCount(); i++) {
                gini -= Math.pow(colTotals[i] / total, 2);
            }

            // compute after split gini impurity for each test level
            for (int i = 0; i < dt.rowCount(); i++) {
                double gini_k = 1;
                for (int j = 0; j < dt.colCount(); j++) {
                    if (rowTotals[i] > 0)
                        gini_k -= Math.pow(dt.get(i, j) / rowTotals[i], 2);
                }
                gini -= gini_k * rowTotals[i] / total;
            }
            return gini;
        }
    },
    ChiSquare {
        @Override
        public double compute(DensityTable<String, String> dt) {
            return 1 - ChiSqIndependence.from(dt, false).pValue();
        }
    };

    public abstract double compute(DensityTable<String, String> dt);
}
