/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.ml.classifier.tree.ctree;

import rapaio.ml.classifier.tools.DensityTable;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeTestFunction extends Serializable {
    String name();

    double compute(DensityTable dt);

    int sign();


    CTreeTestFunction ENTROPY = new CTreeTestFunction() {

        @Override
        public double compute(DensityTable dt) {
            return dt.getSplitEntropy(false);
        }

        @Override
        public String name() {
            return "ENTROPY";
        }

        @Override
        public int sign() {
            return 1;
        }
    };

    CTreeTestFunction INFO_GAIN = new CTreeTestFunction() {
        @Override
        public String name() {
            return "INFO_GAIN";
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DensityTable dt) {
            return dt.getInfoGain(false);
        }
    };

    CTreeTestFunction GAIN_RATIO = new CTreeTestFunction() {
        @Override
        public String name() {
            return "GAIN_RATIO";
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DensityTable dt) {
            return dt.getGainRatio();
        }
    };

    CTreeTestFunction GINI_GAIN = new CTreeTestFunction() {
        @Override
        public String name() {
            return "GINI_GAIN";
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DensityTable dt) {
            return dt.getGiniIndex();
        }
    };

}

