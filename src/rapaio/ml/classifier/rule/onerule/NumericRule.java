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

package rapaio.ml.classifier.rule.onerule;

import rapaio.core.tools.DensityVector;

import static rapaio.printer.format.Format.floatFlexLong;

/**
 * Rule for one rule model for numeric variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/15.
 */
public class NumericRule extends Rule {

    private static final long serialVersionUID = -1337243759410336845L;
    private final double minValue;
    private final double maxValue;
    private final boolean missingValue;

    public NumericRule(double minValue, double maxValue, boolean missingValue, String targetLevel, DensityVector dv) {
        super(targetLevel, dv);
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
        StringBuilder sb = new StringBuilder();
        sb.append("NumericRule {");
        if(missingValue) {
            sb.append("missing=true,");
        } else {
            sb.append("minValue=").append(floatFlexLong(minValue)).append(",");
            sb.append("maxValue=").append(floatFlexLong(maxValue)).append(",");
        }
        sb.append("class=").append(targetLevel).append(",");
        sb.append("errors=").append(floatFlexLong(getErrorCount())).append(",");
        sb.append("total=").append(floatFlexLong(getTotalCount())).append(",");
        sb.append("accuracy=").append(floatFlexLong(getAcc()));
        sb.append("}");
        return sb.toString();
    }
}
