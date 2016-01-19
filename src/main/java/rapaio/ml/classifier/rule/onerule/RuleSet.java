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

import rapaio.sys.WS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Set of rules for one rule algorithm, one rule algorithm builds all possible rule sets and
 * select only the best one for prediction.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/15.
 */
public class RuleSet implements Serializable {

    private static final long serialVersionUID = 9205593021518165406L;

    final String varName;
    final List<Rule> rules = new ArrayList<>();

    public RuleSet(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public double getAccuracy() {
        double total = 0;
        double err = 0;
        for (Rule rule : rules) {
            total += rule.getTotalCount();
            err += rule.getErrorCount();
        }
        return (total - err) / total;
    }

    @Override
    public String toString() {
        return "RuleSet {" + "var=" + varName + ", acc=" + WS.formatFlex(getAccuracy()) + "}";
    }
}
