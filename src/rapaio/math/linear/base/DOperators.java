/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.base;

import jdk.incubator.vector.VectorOperators;

public interface DOperators {

    interface Binary extends DOperators {
        VectorOperators.Associative op();

        double apply(double a, double b);
    }

    Binary ADD = new Binary() {
        @Override
        public VectorOperators.Associative op() {
            return VectorOperators.ADD;
        }

        @Override
        public double apply(double a, double b) {
            return a + b;
        }
    };

    Binary MUL = new Binary() {

        @Override
        public VectorOperators.Associative op() {
            return VectorOperators.MUL;
        }

        @Override
        public double apply(double a, double b) {
            return a * b;
        }
    };
}
