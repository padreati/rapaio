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

package rapaio.ml.classifier.rule.onerule;

import rapaio.core.tools.DVector;
import rapaio.sys.WS;

/**
 * Rule for one rule algoritm for numeric variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/15.
 */
public class NumericRule extends Rule {

    private static final long serialVersionUID = -1337243759410336845L;
    private final double minValue;
    private final double maxValue;
    private final boolean missingValue;

    public NumericRule(double minValue, double maxValue, boolean missingValue, int targetIndex, DVector dv) {
        super(targetIndex, dv);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.missingValue = missingValue;
    }

    public double getMinValue() {
        return minValue;
    }

    public double getMaxValue() {
        return maxValue;
    }

    public boolean isMissingValue() {
        return missingValue;
    }

    @Override
    public String toString() {
        if (missingValue) {
            return "NumericRule {"
                    + "missing=true"
                    + ", class=" + dv.label(targetIndex)
                    + ", errors=" + WS.formatFlex(getErrorCount())
                    + ", total=" + WS.formatFlex(getTotalCount())
                    + ", acc=" + WS.formatFlex(getAcc())
                    + " }";
        }
        return "NumericRule {"
                + "min=" + WS.formatFlex(minValue)
                + ", max=" + WS.formatFlex(maxValue)
                + ", class=" + dv.label(targetIndex)
                + ", errors=" + WS.formatFlex(getErrorCount())
                + ", total=" + WS.formatFlex(getTotalCount())
                + ", acc=" + WS.formatFlex(getAcc())
                + " }";
    }
}
