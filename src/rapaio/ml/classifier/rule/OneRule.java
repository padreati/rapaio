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
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OneRule extends AbstractClassifierModel<OneRule, ClassifierResult<OneRule>> {

    public static OneRule newModel() {
        return new OneRule();
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

    private static final long serialVersionUID = 6220103690711818091L;
    private static final Logger log = Logger.getLogger(OneRule.class.getName());

    private MissingHandler missingHandler = MissingHandler.MAJORITY;
    private Binning binning = new HolteBinning(3);
    private RuleSet bestRuleSet;
    private DensityVector<String> missingDensity;

    private OneRule() {
    }

    @Override
    public String name() {
        return "OneRule";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("OneRule{");
        sb.append("missingHandler=").append(getMissingHandler().name()).append(",");
        sb.append("binning=").append(getBinning().name());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public OneRule newInstance() {
        return newInstanceDecoration(new OneRule())
                .withMissingHandler(getMissingHandler())
                .withBinning(getBinning());
    }

    public MissingHandler getMissingHandler() {
        return missingHandler;
    }

    public OneRule withMissingHandler(MissingHandler missingHandler) {
        this.missingHandler = missingHandler;
        return this;
    }

    public Binning getBinning() {
        return binning;
    }

    public OneRule withBinning(Binning binning) {
        this.binning = binning;
        return this;
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
                    ruleSet = binning.compute(testCol, this, df, weights);
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
    protected ClassifierResult<OneRule> corePredict(final Frame test, final boolean withClasses, final boolean withDensities) {

        if (bestRuleSet == null) {
            log.severe("Best rule not found. Either the classifier was not trained, either something went wrong.");
            throw new IllegalStateException("Best rule not found. Either the classifier was not trained, either something went wrong.");
        }

        ClassifierResult<OneRule> pred = ClassifierResult.build(this, test, withClasses, withDensities);
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
        if (missing && missingHandler.equals(MissingHandler.MAJORITY)) {
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
    public String toSummary(Printer printer, POption... options) {
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
    public String toContent(Printer printer, POption... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption... options) {
        return toSummary(printer, options);
    }
}

