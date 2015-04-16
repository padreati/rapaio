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
 */

package rapaio.ml.classifier.tree.impl;

import rapaio.ml.classifier.tools.DensityTable;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeTestFunction extends Serializable {

    String name();

    CTreeTestFunction newInstance();

    double compute(DensityTable dt);

    int sign();


    public static class Entropy implements CTreeTestFunction {

        @Override
        public String name() {
            return "Entropy";
        }

        @Override
        public CTreeTestFunction newInstance() {
            return new Entropy();
        }

        @Override
        public double compute(DensityTable dt) {
            return dt.getSplitEntropy(false);
        }

        @Override
        public int sign() {
            return 1;
        }
    };

    public static class InfoGain implements CTreeTestFunction {

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
        public double compute(DensityTable dt) {
            return dt.getInfoGain(false);
        }
    };

    public static class GainRatio implements CTreeTestFunction {

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
        public double compute(DensityTable dt) {
            return dt.getGainRatio();
        }
    };

    public static class GiniGain implements CTreeTestFunction {

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
        public double compute(DensityTable dt) {
            return dt.getGiniIndex();
        }
    };
}

