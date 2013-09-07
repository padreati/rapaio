/*
 * Copyright 2013 Aurelian Tutuianu
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

package sample;

import rapaio.data.Vector;
import rapaio.distributions.DUniform;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sandbox {

    public static void main(String[] args) {
        simulation();
    }

    public static void simulation() {

        DUniform distr = new DUniform(1, 6);
        double first = 0;
        double second = 0;
        Vector v = distr.sample(10000000);
        for (int i = 0; i < v.getRowCount(); i++) {
            if (i % 1000 == 0) {
                System.out.print(String.format("skip=%.2f, notskip=%.2f, ", first / i, second / i));
                System.out.println();
            }
            if (v.getIndex(i) == 1) {
                continue;
            }
            if (v.getIndex(i) == 2) {
                second++;
                continue;
            }
            if (v.getIndex(i) == 3) {
                first++;
                continue;
            }
            first++;
            second++;
        }
    }
}
