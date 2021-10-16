/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.rule;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.supervised.ClassifierHookInfo;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierResult;
import rapaio.ml.supervised.rule.onerule.HolteBinning;
import rapaio.ml.supervised.rule.onerule.NominalRule;
import rapaio.ml.supervised.rule.onerule.NumericRule;
import rapaio.ml.supervised.rule.onerule.Rule;
import rapaio.ml.supervised.rule.onerule.RuleSet;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends ClassifierModel<OneRule, ClassifierResult, ClassifierHookInfo> {

    public static OneRule newModel() {
        return new OneRule();
    }

    @Serial
    private static final long serialVersionUID = 6220103690711818091L;
    private static final Logger log = Logger.getLogger(OneRule.class.getName());

    private static final Binning DEFAULT_BINNING = new HolteBinning(3);

    /**
     * Method of handling missing values
     */
    public final ValueParam<MissingHandler, OneRule> missingHandler =
            new ValueParam<>(this, MissingHandler.MAJORITY, "missingHandler", Objects::nonNull);

    /**
     * Binning method used in the algorithm.
     */
    public final ValueParam<Binning, OneRule> binning = new ValueParam<>(this, DEFAULT_BINNING,
            "binning",Objects::nonNull);

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
        return new Capabilities(
                1, 1_000_000,
                List.of(VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.DOUBLE, VarType.LONG), true,
                1, 1, List.of(VarType.NOMINAL), false);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        bestRuleSet = null;
        for (String testCol : inputNames()) {
            RuleSet ruleSet = switch (df.rvar(testCol).type()) {
                case INT, DOUBLE, LONG -> binning.get().compute(testCol, this, df, weights);
                default -> buildNominal(testCol, df, weights);
            };
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
                pred.firstClasses().setLabel(i, p.v1);
            }
            if (withDensities) {
                List<String> targetLevels = firstTargetLevels();
                DensityVector<String> density = p.v2.copy().normalize();
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
            case INT, DOUBLE, LONG -> {
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
            }
            default -> {
                String label = df.getLabel(row, testVar);
                for (Rule oneRule : bestRuleSet.getRules()) {
                    NominalRule nomRule = (NominalRule) oneRule;
                    if (nomRule.getTestLabel().equals(label)) {
                        return Pair.from(nomRule.getTargetClass(), nomRule.getDensityVector());
                    }
                }
            }
        }
        return Pair.from("?", DensityVector.emptyByLabels(true, firstTargetLevels()));
    }

    @SuppressWarnings("unchecked")
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

