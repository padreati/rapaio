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

package rapaio.core.stat;

import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static rapaio.core.CoreTools.mean;
import static rapaio.sys.WS.formatFlex;

/**
 * Compute covariance of two variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/15.
 */
public class Covariance implements Printable {

    public static Covariance from(Var var1, Var var2) {
        return new Covariance(var1, var2);
    }

    private final String varName1;
    private final String varName2;
    private final double value;
    private int completeCount;
    private int missingCount;

    private Covariance(Var var1, Var var2) {
        this.varName1 = var1.getName();
        this.varName2 = var2.getName();
        this.value = compute(var1, var2);
    }

    private double compute(final Var x, final Var y) {

        Mapping map = Mapping.wrap(IntStream.range(0, Math.min(x.getRowCount(), y.getRowCount())).filter(row -> !x.isMissing(row) && !y.isMissing(row)).boxed().collect(toList()));
        completeCount = map.size();
        missingCount = Math.max(x.getRowCount(), y.getRowCount()) - completeCount;

        if (map.size() < 2) {
            return 0;
        }

        Var xx = x.mapRows(map);
        Var yy = y.mapRows(map);

        double m1 = mean(xx).getValue();
        double m2 = mean(yy).getValue();
        double cov = 0;
        for (int i = 0; i < completeCount; i++) {
            cov += (xx.getValue(i) - m1) * (yy.getValue(i) - m2);
        }
        return cov / (completeCount - 1.0);
    }

    public double getValue() {
        return value;
    }

    @Override
    public String getSummary() {
        return "\n" +
                "> cov[" + varName1 + ", " + varName2 + "]\n" +
                "total rows: " + (completeCount + missingCount) + " (complete: " + completeCount + ", missing: " + missingCount + " )\n" +
                "covariance: " + formatFlex(value) + "\n";
    }
}
