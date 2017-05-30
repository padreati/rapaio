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

package rapaio.ml.classifier.rule.onerule;

import rapaio.core.tools.DVector;
import rapaio.sys.WS;

/**
 * Rule from one rule based algorithm for nominal variables
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/15.
 */
public class NominalRule extends Rule {

    private static final long serialVersionUID = -6974686908599235407L;

    private final String testLabel;

    public NominalRule(String testLabel, int targetIndex, DVector dv) {
        super(targetIndex, dv);
        this.testLabel = testLabel;
    }

    public String getTestLabel() {
        return testLabel;
    }

    @Override
    public String toString() {
        return "NominalRule {"
                + "value=" + testLabel
                + ", class=" + dv.getLabel(targetIndex)
                + ", errors=" + WS.formatFlex(getErrorCount())
                + ", total=" + WS.formatFlex(getTotalCount())
                + ", acc=" + WS.formatFlex(getAcc())
                + '}';
    }
}
