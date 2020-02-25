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

package rapaio.experiment.ml.classifier.tree;

import rapaio.core.tests.ChiSqIndependence;
import rapaio.core.tools.DensityTable;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public interface CTreePurityFunction extends Serializable {

    String name();

    double compute(DensityTable<String, String> dt);

    CTreePurityFunction InfoGain = new CTreePurityFunction() {
        private static final long serialVersionUID = 152790997381399918L;

        @Override
        public double compute(DensityTable<String, String> dt) {
            return dt.splitByRowInfoGain();
        }

        @Override
        public String name() {
            return "InfoGain";
        }

        @Override
        public String toString() {
            return name();
        }
    };
    CTreePurityFunction GainRatio = new CTreePurityFunction() {
        private static final long serialVersionUID = -2478996054579932911L;

        @Override
        public String name() {
            return "GainRatio";
        }

        @Override
        public double compute(DensityTable<String, String> dt) {
            return dt.splitByRowGainRatio();
        }

        @Override
        public String toString() {
            return name();
        }
    };

    CTreePurityFunction GiniGain = new CTreePurityFunction() {
        private static final long serialVersionUID = 3547209320599654633L;

        @Override
        public String name() {
            return "GiniGain";
        }

        @Override
        public double compute(DensityTable<String, String> dt) {
            double[] rowTotals = new double[dt.rowCount()];
            double[] colTotals = new double[dt.colCount()];
            double total = 0.0;
            for (int i = 0; i < dt.rowCount(); i++) {
                // j = 1 just for optimization, we know that we should not have missing targets
                for (int j = 0; j < dt.colCount(); j++) {
                    rowTotals[i] += dt.get(i, j);
                    colTotals[j] += dt.get(i, j);
                    total += dt.get(i, j);
                }
            }
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

        @Override
        public String toString() {
            return name();
        }
    };
    CTreePurityFunction ChiSquare = new CTreePurityFunction() {
        private static final long serialVersionUID = 3547209320599654633L;

        @Override
        public String name() {
            return "ChiSquare";
        }

        @Override
        public double compute(DensityTable<String, String> dt) {
            return 1 - ChiSqIndependence.from(dt, false).pValue();
        }

        @Override
        public String toString() {
            return name();
        }
    };

}
