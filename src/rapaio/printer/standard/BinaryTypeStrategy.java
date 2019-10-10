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

package rapaio.printer.standard;

import rapaio.data.Frame;
import rapaio.data.Var;

public class BinaryTypeStrategy implements TypeStrategy {

    public void getVarSummary(Frame df, Var v, String[][] first, String[][] second, int th) {
        first[th][0] = "0";
        first[th][1] = "1";
        first[th][2] = "NA's";

        int ones = 0;
        int zeros = 0;
        int missing = 0;
        for (int j = 0; j < v.rowCount(); j++) {
            if (v.isMissing(j)) {
                missing++;
            } else {
                if (v.getInt(j) == 1) {
                    ones++;
                }
                else {
                    zeros++;
                }
            }
        }
        second[th][0] = String.valueOf(zeros);
        second[th][1] = String.valueOf(ones);
        second[th][2] = String.valueOf(missing);
    }

    @Override
    public void getPrintSummary(Var v, String[] first, String[] second) {
        first[0] = "0";
        first[1] = "1";
        first[2] = "NA's";

        int ones = 0;
        int zeros = 0;
        int missing = 0;
        for (int i = 0; i < v.rowCount(); i++) {
            if (v.isMissing(i)) {
                missing++;
            } else {
                if (v.getInt(i) == 1) {
                    ones++;
                }
                else {
                    zeros++;
                }
            }
        }
        second[0] = String.valueOf(zeros);
        second[1] = String.valueOf(ones);
        second[2] = String.valueOf(missing);
    }


}
