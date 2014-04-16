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

package rapaio.classifier.tree;

import rapaio.classifier.tools.DensityTable;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public enum CTreeFunction {

    ENTROPY(1) {
        @Override
        public double compute(DensityTable dt) {
            return dt.getSplitEntropy(false);
        }
    },
    INFO_GAIN(-1) {
        @Override
        public double compute(DensityTable dt) {
            return dt.getInfoGain(false);
        }
    },
    GAIN_RATIO(-1) {
        @Override
        public double compute(DensityTable dt) {
            return dt.getGainRatio();
        }
    },
    GINI(-1) {
        @Override
        public double compute(DensityTable dt) {
            return dt.getGiniIndex();
        }
    };

    private final int sign;

    CTreeFunction(int sign) {
        this.sign = sign;
    }

    public abstract double compute(DensityTable dt);

    public int sign() {
        return sign;
    }
}
