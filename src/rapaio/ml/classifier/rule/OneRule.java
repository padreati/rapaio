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

package rapaio.ml.classifier.rule;

import rapaio.core.tools.*;
import rapaio.data.*;
import rapaio.data.filter.var.*;
import rapaio.ml.classifier.*;
import rapaio.ml.classifier.rule.onerule.*;
import rapaio.ml.common.*;
import rapaio.printer.*;
import rapaio.printer.format.*;
import rapaio.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifier implements Printable {

    private static final long serialVersionUID = 6220103690711818091L;

    private static final Logger log = Logger.getLogger(OneRule.class.getName());

    private double minCount = 6;
    private RuleSet bestRuleSet;

    @Override
    public String name() {
        return "OneRule";
    }

    @Override
    public String fullName() {
        return String.format("OneRule (minCount=%s)", Format.floatFlex(minCount));
    }

    @Override
    public OneRule newInstance() {
        return new OneRule().withMinCount(minCount);
    }

    public OneRule withMinCount(double minCount) {
        this.minCount = minCount;
        return this;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1)
                .withInputTypes(VType.BINARY, VType.INT, VType.NOMINAL, VType.DOUBLE, VType.LONG)
                .withTargetTypes(VType.NOMINAL)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        bestRuleSet = null;
        for (String testCol : inputNames()) {
            RuleSet ruleSet;
            switch (df.rvar(testCol).type()) {
                case BINARY:
                case INT:
                case DOUBLE:
                case LONG:
                    ruleSet = buildNumeric(testCol, df, weights);
                    break;
                default:
                    ruleSet = buildNominal(testCol, df, weights);
            }
            if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
                bestRuleSet = ruleSet;
            }
        }
        return true;
    }

    @Override
    protected CPrediction corePredict(final Frame test, final boolean withClasses, final boolean withDensities) {
        CPrediction pred = CPrediction.build(this, test, withClasses, withDensities);
        for (int i = 0; i < test.rowCount(); i++) {
            Pair<String, DVector> p = predict(test, i);
            if (withClasses) {
                pred.firstClasses().setLabel(i, p._1);
            }
            if (withDensities) {
                List<String> dict = firstTargetLevels();
                DVector dv = p._2.solidCopy();
                dv.normalize();
                for (int j = 0; j < dict.size(); j++) {
                    pred.firstDensity().setDouble(i, j, dv.get(j));
                }
            }
        }
        return pred;
    }

    private Pair<String, DVector> predict(Frame df, int row) {
        if (bestRuleSet == null) {
            log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
            return Pair.from("?", DVector.empty(true, firstTargetLevels().size()));
        }
        String testVar = bestRuleSet.getVarName();
        switch (df.rvar(testVar).type()) {
            case BINARY:
            case INT:
            case DOUBLE:
            case LONG:
                boolean missing = df.rvar(testVar).isMissing(row);
                double value = df.getDouble(row, testVar);
                for (Rule oneRule : bestRuleSet.getRules()) {
                    NumericRule numRule = (NumericRule) oneRule;
                    if (missing && numRule.isMissingValue()) {
                        return Pair.from(numRule.getTargetClass(), numRule.getDV());
                    }
                    if (!missing && !numRule.isMissingValue() && value >= numRule.getMinValue() && value <= numRule.getMaxValue()) {
                        return Pair.from(numRule.getTargetClass(), numRule.getDV());
                    }
                }
                break;
            default:
                String label = df.getLabel(row, testVar);
                for (Rule oneRule : bestRuleSet.getRules()) {
                    NominalRule nomRule = (NominalRule) oneRule;
                    if (nomRule.getTestLabel().equals(label)) {
                        return Pair.from(nomRule.getTargetClass(), nomRule.getDV());
                    }
                }
        }
        return Pair.from("?", DVector.empty(true, firstTargetLevels().size()));
    }

    private RuleSet buildNominal(String testVar, Frame df, Var weights) {
        RuleSet set = new RuleSet(testVar);

        List<String> testDict = df.rvar(testVar).levels();
        List<String> targetDict = firstTargetLevels();

        DVector[] dvs = IntStream.range(0, testDict.size()).boxed().map(i -> DVector.empty(false, targetDict)).toArray(DVector[]::new);
        df.stream().forEach(s -> dvs[df.getInt(s.row(), testVar)].increment(df.getInt(s.row(), firstTargetName()), weights.getDouble(s.row())));
        for (int i = 0; i < testDict.size(); i++) {
            DVector dv = dvs[i];
            int bestIndex = dv.findBestIndex();
            set.getRules().add(new NominalRule(testDict.get(i), bestIndex, dv));
        }
        return set;
    }

    private RuleSet buildNumeric(String testCol, Frame df, Var weights) {
        RuleSet set = new RuleSet(testCol);
        Var sort = new VRefSort(RowComparators.doubleComparator(df.rvar(testCol), true),
                RowComparators.labelComparator(df.rvar(firstTargetName()), true)).fapply(VarInt.seq(weights.rowCount()));
        int pos = 0;
        while (pos < sort.rowCount()) {
            if (df.isMissing(sort.getInt(pos), testCol)) {
                pos++;
                continue;
            }
            break;
        }

        // first process missing values
        if (pos > 0) {
            DVector hist = DVector.empty(true, firstTargetLevels());
            for (int i = 0; i < pos; i++) {
                hist.increment(df.getInt(sort.getInt(i), firstTargetName()), weights.getDouble(sort.getInt(i)));
            }
            List<Integer> best = new ArrayList<>();
            double max = Double.MIN_VALUE;
            int next = hist.findBestIndex();
            set.getRules().add(new NumericRule(Double.NaN, Double.NaN, true, next, hist));
        }

        // now learn numeric intervals
        List<NumericRule> candidates = new ArrayList<>();

        //splits from same value
        int i = pos;
        int index;
        while (i < sort.rowCount()) {
            // start a new bucket
            int startIndex = i;
            DVector hist = DVector.empty(true, firstTargetLevels());

            do { // fill it until it has enough of the majority class
                index = df.getInt(sort.getInt(i), firstTargetName());
                hist.increment(index, weights.getDouble(sort.getInt(i)));
                i++;
            } while (hist.get(index) < minCount && i < sort.rowCount());

            // while class remains the same, keep on filling
            while (i < sort.rowCount()) {
                index = sort.getInt(i);
                if (df.getInt(sort.getInt(i), firstTargetName()) == index) {
                    hist.increment(index, weights.getDouble(sort.getInt(i)));
                    i++;
                    continue;
                }
                break;
            }
            // keep on while attr value is the same
            while (i < sort.rowCount()
                    && df.getDouble(sort.getInt(i - 1), testCol)
                    == df.getDouble(sort.getInt(i), testCol)) {
                index = df.getInt(sort.getInt(i), firstTargetName());
                hist.increment(index, weights.getDouble(sort.getInt(i)));
                i++;
            }
            int next = hist.findBestIndex();
            double minValue = Double.NEGATIVE_INFINITY;
            if (startIndex != pos) {
                minValue = (df.getDouble(sort.getInt(startIndex), testCol)
                        + df.getDouble(sort.getInt(startIndex - 1), testCol)) / 2.;
            }
            double maxValue = Double.POSITIVE_INFINITY;
            if (i != sort.rowCount()) {
                maxValue = (df.getDouble(sort.getInt(i - 1), testCol) + df.getDouble(sort.getInt(i), testCol)) / 2;
            }

            candidates.add(new NumericRule(minValue, maxValue, false, next, hist));
        }

        NumericRule last = null;
        for (NumericRule rule : candidates) {
            if (last == null) {
                last = rule;
                continue;
            }
            if (last.getTargetClass().equals(rule.getTargetClass())) {
                DVector dv = last.getDV().solidCopy();
                dv.plus(rule.getDV(), 1);
                last = new NumericRule(last.getMinValue(), rule.getMaxValue(), false, last.getTargetIndex(), dv);
            } else {
                set.getRules().add(last);
                last = rule;
            }
        }

        set.getRules().add(last);
        return set;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("OneRule model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        sb.append("Best").append(bestRuleSet.toString()).append("\n");
        for (Rule rule : bestRuleSet.getRules()) {
            sb.append("> ").append(rule.toString()).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String content() {
        return null;
    }

    @Override
    public String fullContent() {
        return null;
    }

}

