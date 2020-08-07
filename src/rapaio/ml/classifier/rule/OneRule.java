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

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.rule.onerule.HolteBinning;
import rapaio.ml.classifier.rule.onerule.NominalRule;
import rapaio.ml.classifier.rule.onerule.NumericRule;
import rapaio.ml.classifier.rule.onerule.Rule;
import rapaio.ml.classifier.rule.onerule.RuleSet;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifierModel<OneRule, ClassifierResult> {

    public static OneRule newModel() {
        return new OneRule();
    }

    private static final long serialVersionUID = 6220103690711818091L;
    private static final Logger log = Logger.getLogger(OneRule.class.getName());

    private static final Binning DEFAULT_BINNING = new HolteBinning(3);

    public final ValueParam<MissingHandler, OneRule> missingHandler = new ValueParam<>(this, MissingHandler.MAJORITY,
            "missingHandler",
            "Method of handling missing values",
            Objects::nonNull);

    public final ValueParam<Binning, OneRule> binning = new ValueParam<>(this, DEFAULT_BINNING,
            "binning",
            "Binning method used in the algorithm.",
            Objects::nonNull);

    private RuleSet bestRuleSet;
    private DensityVector<String> missingDensity;

    private OneRule() {
    }

    @Override
    public String name() {
        return "OneRule";
    }

    @Override
    public OneRule newInstance() {
        return new OneRule().copyParameterValues(this);
    }

    /**
     * Gets best fitted rule set.
     *
     * @return best fitted rule set
     */
    public RuleSet getBestRuleSet() {
        return bestRuleSet;
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .minInputCount(1)
                .maxInputCount(1_000_000)
                .minTargetCount(1)
                .maxTargetCount(1)
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.NOMINAL, VType.DOUBLE, VType.LONG))
                .targetType(VType.NOMINAL)
                .allowMissingInputValues(true)
                .allowMissingTargetValues(false)
                .build();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        bestRuleSet = null;
        for (String testCol : inputNames()) {
            RuleSet ruleSet;
            switch (df.rvar(testCol).type()) {
                case INT:
                case DOUBLE:
                case LONG:
                    ruleSet = binning.get().compute(testCol, this, df, weights);
                    break;
                default:
                    ruleSet = buildNominal(testCol, df, weights);
            }
            if (bestRuleSet == null || ruleSet.getAccuracy() > bestRuleSet.getAccuracy()) {
                bestRuleSet = ruleSet;
            }
        }

        missingDensity = DensityVector.fromLevelWeights(false, df.rvar(firstTargetName()), weights);

        return bestRuleSet != null;
    }

    @Override
    protected ClassifierResult corePredict(final Frame test, final boolean withClasses, final boolean withDensities) {

        if (bestRuleSet == null) {
            log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
            throw new IllegalStateException("Best rule not found. Either the classifier was not trained, either something went wrong.");
        }

        ClassifierResult pred = ClassifierResult.build(this, test, withClasses, withDensities);
        for (int i = 0; i < test.rowCount(); i++) {
            Pair<String, DensityVector<String>> p = predict(test, i);
            if (withClasses) {
                pred.firstClasses().setLabel(i, p._1);
            }
            if (withDensities) {
                List<String> targetLevels = firstTargetLevels();
                DensityVector<String> density = p._2.copy().normalize();
                for (int j = 1; j < targetLevels.size(); j++) {
                    pred.firstDensity().setDouble(i, j, density.get(targetLevels.get(j)));
                }
            }
        }
        return pred;
    }

    private Pair<String, DensityVector<String>> predict(Frame df, int row) {
        String testVar = bestRuleSet.getVarName();

        boolean missing = df.rvar(testVar).isMissing(row);
        if (missing && missingHandler.get().equals(MissingHandler.MAJORITY)) {
            return Pair.from(testVar, missingDensity);
        }

        switch (df.rvar(testVar).type()) {
            case INT:
            case DOUBLE:
            case LONG:
                double value = df.getDouble(row, testVar);
                for (Rule oneRule : bestRuleSet.getRules()) {
                    NumericRule numRule = (NumericRule) oneRule;
                    if (missing && numRule.isMissingValue()) {
                        return Pair.from(numRule.getTargetClass(), numRule.getDensityVector());
                    }
                    if (!missing && !numRule.isMissingValue() && value >= numRule.getMinValue() && value <= numRule.getMaxValue()) {
                        return Pair.from(numRule.getTargetClass(), numRule.getDensityVector());
                    }
                }
                break;
            default:
                String label = df.getLabel(row, testVar);
                for (Rule oneRule : bestRuleSet.getRules()) {
                    NominalRule nomRule = (NominalRule) oneRule;
                    if (nomRule.getTestLabel().equals(label)) {
                        return Pair.from(nomRule.getTargetClass(), nomRule.getDensityVector());
                    }
                }
        }
        return Pair.from("?", DensityVector.emptyByLabels(true, firstTargetLevels()));
    }

    private RuleSet buildNominal(String testVarName, Frame df, Var weights) {
        RuleSet set = new RuleSet(testVarName);

        List<String> testDict = df.rvar(testVarName).levels();
        List<String> targetDict = firstTargetLevels();

        DensityVector<String>[] densityVectors = new DensityVector[testDict.size()];
        for (int i = 0; i < densityVectors.length; i++) {
            densityVectors[i] = DensityVector.emptyByLabels(false, targetDict);
        }

        int testIndex = df.varIndex(testVarName);
        int targetIndex = df.varIndex(firstTargetName());
        for (int i = 0; i < df.rowCount(); i++) {
            densityVectors[df.getInt(i, testIndex)].increment(df.getLabel(i, targetIndex), weights.getDouble(i));
        }

        for (int i = 0; i < testDict.size(); i++) {
            DensityVector<String> dv = densityVectors[i];
            int bestIndex = dv.findBestIndex();
            set.getRules().add(new NominalRule(testDict.get(i), dv.index().getValue(bestIndex), dv));
        }
        return set;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append(", fitted=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", rule set: ").append(bestRuleSet.toString());
            for (Rule rule : bestRuleSet.getRules()) {
                sb.append(", ").append(rule.toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(fullNameSummary());
        sb.append(capabilitiesSummary());

        sb.append("Model fitted: ").append(hasLearned()).append("\n");

        if (!hasLearned()) {
            return sb.toString();
        }

        sb.append(inputVarsSummary(printer, options));
        sb.append(targetVarsSummary());

        sb.append("Best").append(bestRuleSet.toString()).append("\n");
        for (Rule rule : bestRuleSet.getRules()) {
            sb.append("> ").append(rule.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    public enum MissingHandler {
        /**
         * Missing values are treated as a separate category and the prediction is computed only
         * from the target values which corresponds to missing test values.
         */
        CATEGORY,
        /**
         * Missing values are ignored from calculations and at prediction time when a missing
         * value is encountered in test variable the predicted value is the overall training
         * set maximal value.
         */
        MAJORITY
    }

    public interface Binning extends Serializable {

        String name();

        RuleSet compute(String testVarName, OneRule parent, Frame df, Var weights);
    }
}

