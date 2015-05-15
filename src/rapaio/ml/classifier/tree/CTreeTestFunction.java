/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import rapaio.core.tools.DTable;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
@Deprecated
public interface CTreeTestFunction extends Serializable {

    String name();

    CTreeTestFunction newInstance();

    double compute(DTable dt);

    int sign();

    class Entropy implements CTreeTestFunction {

        private static final long serialVersionUID = 6818050916634872153L;

        @Override
        public String name() {
            return "Entropy";
        }

        @Override
        public CTreeTestFunction newInstance() {
            return new Entropy();
        }

        @Override
        public double compute(DTable dt) {
            return dt.getSplitEntropy(false);
        }

        @Override
        public int sign() {
            return 1;
        }
    };

    class InfoGain implements CTreeTestFunction {

        private static final long serialVersionUID = -632310330804645110L;

        @Override
        public String name() {
            return "InfoGain";
        }

        @Override
        public CTreeTestFunction newInstance() {
            return new InfoGain();
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DTable dt) {
            return dt.getInfoGain(false);
        }
    };

    class GainRatio implements CTreeTestFunction {

        private static final long serialVersionUID = 4570579508597802935L;

        @Override
        public String name() {
            return "GainRatio";
        }

        @Override
        public CTreeTestFunction newInstance() {
            return new GainRatio();
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DTable dt) {
            return dt.getGainRatio(false);
        }
    };

    class GiniGain implements CTreeTestFunction {

        private static final long serialVersionUID = 3527719507451545704L;

        @Override
        public String name() {
            return "GiniGain";
        }

        @Override
        public CTreeTestFunction newInstance() {
            return new GiniGain();
        }

        @Override
        public int sign() {
            return -1;
        }

        @Override
        public double compute(DTable dt) {
            return dt.getGiniIndex();
        }
    };
}

