/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree;

import rapaio.core.tests.ChiSquareTest;
import rapaio.core.tools.DTable;
import rapaio.util.Tagged;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreePurityFunction extends Tagged, Serializable {

    CTreePurityFunction InfoGain = new CTreePurityFunction() {
        private static final long serialVersionUID = 152790997381399918L;

        @Override
        public double compute(DTable dt) {
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
        public double compute(DTable dt) {
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
        public double compute(DTable dt) {
            double[] rowTotals = new double[dt.getRowLevels().length];
            double[] colTotals = new double[dt.getColLevels().length];
            double total = 0.0;
            for (int i = 0; i < dt.getRowLevels().length; i++) {
                // j = 1 just for optimization, we know that we should not have missing targets
                for (int j = 1; j < dt.getColLevels().length; j++) {
                    rowTotals[i] += dt.get(i, j);
                    if (i != 0) {
                        colTotals[j] += dt.get(i, j);
                        total += dt.get(i, j);
                    }
                }
            }
            if (total <= 0) {
                // no instances | all missing on test
                return 0;
            }

            // compute before split gini impurity
            double gini = 1.0;
            for (int i = 1; i < dt.getColLevels().length; i++) {
                gini -= Math.pow(colTotals[i] / total, 2);
            }

            // compute after split gini impurity for each test level
            for (int i = 1; i < dt.getRowLevels().length; i++) {
                double gini_k = 1;
                for (int j = 1; j < dt.getColLevels().length; j++) {
                    if (rowTotals[i] > 0)
                        gini_k -= Math.pow(dt.get(i, j) / rowTotals[i], 2);
                }
                gini -= gini_k * rowTotals[i] / total;
            }
            return gini * total / (total + rowTotals[0]);
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
        public double compute(DTable dt) {
            return 1 - ChiSquareTest.independenceTest(dt).pValue();
        }

        @Override
        public String toString() {
            return name();
        }
    };

    double compute(DTable dt);

}
