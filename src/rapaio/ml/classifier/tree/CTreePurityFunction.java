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
    };
    CTreePurityFunction GainRatio = new CTreePurityFunction() {
        private static final long serialVersionUID = -2478996054579932911L;

        @Override
        public double compute(DTable dt) {
            return dt.splitByRowGainRatio();
        }

        @Override
        public String name() {
            return "GainRatio";
        }
    };
    CTreePurityFunction GiniGain = new CTreePurityFunction() {
        private static final long serialVersionUID = 3547209320599654633L;

        @Override
        public double compute(DTable dt) {
            return dt.splitByRowGiniGain();
        }

        @Override
        public String name() {
            return "GiniGain";
        }
    };

    double compute(DTable dt);
}
