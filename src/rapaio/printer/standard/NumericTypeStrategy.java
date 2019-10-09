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

import rapaio.core.stat.*;
import rapaio.data.*;

public class NumericTypeStrategy implements TypeStrategy {
	public void getVarSummary(Frame df, Var v,String[][] first, String[][] second, int th ){
		double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
        double[] perc = Quantiles.of(v, p).values();
        double mean = Mean.of(v).value();

        int nas = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        first[th][0] = "Min.";
        first[th][1] = "1st Qu.";
        first[th][2] = "Median";
        first[th][3] = "Mean";
        first[th][4] = "2nd Qu.";
        first[th][5] = "Max.";

        second[th][0] = String.format("%.3f", perc[0]);
        second[th][1] = String.format("%.3f", perc[1]);
        second[th][2] = String.format("%.3f", perc[2]);
        second[th][3] = String.format("%.3f", mean);
        second[th][4] = String.format("%.3f", perc[3]);
        second[th][5] = String.format("%.3f", perc[4]);

        if (nas != 0) {
            first[th][6] = "NA's";
            second[th][6] = String.format("%d", nas);
        }
	}

	@Override
	public void getPrintSummary(Var v, String[] first, String[] second) {
		double[] p = new double[]{0., 0.25, 0.50, 0.75, 1.00};
        double[] perc = Quantiles.of(v, p).values();
        double mean = Mean.of(v).value();

        int nas = 0;
        for (int j = 0; j < v.rowCount(); j++) {
            if (v.isMissing(j)) {
                nas++;
            }
        }

        first[0] = "Min.";
        first[1] = "1st Qu.";
        first[2] = "Median";
        first[3] = "Mean";
        first[4] = "2nd Qu.";
        first[5] = "Max.";

        second[0] = String.format("%.3f", perc[0]);
        second[1] = String.format("%.3f", perc[1]);
        second[2] = String.format("%.3f", perc[2]);
        second[3] = String.format("%.3f", mean);
        second[4] = String.format("%.3f", perc[3]);
        second[5] = String.format("%.3f", perc[4]);

        if (nas != 0) {
            first[6] = "NA's";
            second[6] = String.format("%d", nas);
        }
	}
}
