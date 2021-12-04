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

package rapaio.ml.model.ensemble;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.filter.FRefSort;
import rapaio.data.filter.VShuffle;
import rapaio.data.sample.RowSampler;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.metric.Confusion;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.CTree;
import rapaio.ml.model.tree.ctree.Node;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends ClassifierModel<CForest, ClassifierResult, RunInfo<CForest>> {

    public static CForest newModel() {
        return new CForest();
    }

    @Serial
    private static final long serialVersionUID = -145958939373105497L;

    /**
     * Performs out of the bag computations.
     */
    public final ValueParam<Boolean, CForest> oob = new ValueParam<>(this, false, "oob");

    /**
     * Performs frequency variable importance computations
     */
    public final ValueParam<Boolean, CForest> viFreq = new ValueParam<>(this, false, "freqVI");

    /**
     * Performs gain variable importance computations
     */
    public final ValueParam<Boolean, CForest> viGain = new ValueParam<>(this, false, "gainVI");

    /**
     * Perform permutation variable importance computations
     */
    public final ValueParam<Boolean, CForest> viPerm = new ValueParam<>(this, false, "viPerm");

    /**
     * Model archetype used in ensemble
     */
    public final ValueParam<CTree, CForest> model = new ValueParam<>(this, CTree.newCART()
            .varSelector.set(VarSelector.auto())
            .rowSampler.set(RowSampler.bootstrap()),
            "model");

    /**
     * Bagging mode used to average the results
     */
    public final ValueParam<BaggingMode, CForest> baggingMode = new ValueParam<>(this, BaggingMode.DISTRIBUTION, "bagging");

    // learning artifacts
    private List<ClassifierModel<?, ?, ?>> predictors = new ArrayList<>();
    private double oobError = Double.NaN;
    private DMatrix oobDensities;
    private Var oobPredictedClasses;
    private Var oobTrueClass;

    private final Map<String, List<Double>> freqVIMap = new HashMap<>();
    private final Map<String, List<Double>> gainVIMap = new HashMap<>();
    private final Map<String, List<Double>> permVIMap = new HashMap<>();

    private CForest() {
        rowSampler.set(RowSampler.bootstrap());
    }

    public List<ClassifierModel<?, ?, ?>> getPredictors() {
        return predictors;
    }

    public double getOobError() {
        return oobError;
    }

    public DMatrix getOobDensities() {
        return oobDensities;
    }

    public Var getOobPredictedClasses() {
        return oobPredictedClasses;
    }

    public Var getOobTrueClass() {
        return oobTrueClass;
    }

    @Override
    public String name() {
        return "CForest";
    }

    @Override
    public CForest newInstance() {
        return new CForest().copyParameterValues(this);
    }

    @Override
    public Capabilities capabilities() {
        Capabilities cc = model.get().capabilities();
        return new Capabilities(
                cc.minInputCount(), cc.maxInputCount(), Arrays.asList(cc.inputTypes().toArray(VarType[]::new)),
                cc.allowMissingInputValues(),
                1, 1, List.of(VarType.NOMINAL, VarType.BINARY), false);
    }

    private Frame getVIInfo(Map<String, List<Double>> viMap) {
        Var name = VarNominal.empty().name("name");
        Var score = VarDouble.empty().name("mean");
        Var sd = VarDouble.empty().name("sd");
        for (Map.Entry<String, List<Double>> e : viMap.entrySet()) {
            name.addLabel(e.getKey());
            VarDouble scores = VarDouble.copy(e.getValue());
            sd.addDouble(Variance.of(scores).sdValue());
            score.addDouble(Mean.of(scores).value());
        }
        double maxScore = Maximum.of(score).value();
        Var scaled = VarDouble.from(score.size(), row -> 100.0 * score.getDouble(row) / maxScore).name("scaled score");
        return SolidFrame.byVars(name, score, sd, scaled).fapply(FRefSort.by(score.refComparator(false))).copy();
    }

    public Frame getFreqVIInfo() {
        return getVIInfo(freqVIMap);
    }

    public Frame getGainVIInfo() {
        return getVIInfo(gainVIMap);
    }

    public Frame getPermVIInfo() {
        Var name = VarNominal.empty().name("name");
        Var score = VarDouble.empty().name("mean");
        Var sds = VarDouble.empty().name("sd");
        Var zscores = VarDouble.empty().name("z-score");
        Var pvalues = VarDouble.empty().name("p-value");
        Distribution normal = Normal.std();
        for (Map.Entry<String, List<Double>> e : permVIMap.entrySet()) {
            name.addLabel(e.getKey());
            VarDouble scores = VarDouble.copy(e.getValue());
            double mean = Mean.of(scores).value();
            double sd = Variance.of(scores).sdValue();
            double zscore = mean / (sd);
            double pvalue = normal.cdf(2 * normal.cdf(-Math.abs(zscore)));
            score.addDouble(Math.abs(mean));
            sds.addDouble(sd);
            zscores.addDouble(Math.abs(zscore));
            pvalues.addDouble(pvalue);
        }
        return SolidFrame.byVars(name, score, sds, zscores, pvalues)
                .fapply(FRefSort.by(zscores.refComparator(false))).copy();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        if (oob.get()) {
            oobDensities = DMatrix.fill(df.rowCount(), firstTargetLevels().size() - 1, 0.0);
            oobTrueClass = df.rvar(firstTargetName()).copy();
            oobPredictedClasses = VarNominal.empty(df.rowCount(), firstTargetLevels());
        }
        if (viFreq.get()) {
            freqVIMap.clear();
        }
        if (viGain.get()) {
            gainVIMap.clear();
        }
        if (viPerm.get()) {
            permVIMap.clear();
        }

        // build in parallel the trees, than oob and running hook cannot run at the
        // same moment when weak tree was built
        // for a real running hook behavior run without threading
        predictors = new ArrayList<>();
        IntStream range = IntStream.range(0, runs.get());
        if (poolSize.get() != 0) {
            range = range.parallel();
        }
        List<Pair<ClassifierModel<?, ?, ?>, Mapping>> list = range
                .mapToObj(s -> buildWeakPredictor(df, weights))
                .collect(Collectors.toList());
        for (int i = 0; i < list.size(); i++) {
            Pair<ClassifierModel<?, ?, ?>, Mapping> weak = list.get(i);
            predictors.add(weak.v1);
            if (oob.get()) {
                oobCompute(df, weak);
            }
            if (viFreq.get()) {
                freqVICompute(weak);
            }
            if (viGain.get()) {
                gainVICompute(weak);
            }
            if (viPerm.get()) {
                permVICompute(df, weak);
            }
            runningHook.get().accept(RunInfo.forClassifier(this, i));
        }
        return true;
    }

    private void permVICompute(Frame df, Pair<ClassifierModel<?, ?, ?>, Mapping> weak) {
        ClassifierModel<?, ?, ?> c = weak.v1;
        Mapping oobIndexes = weak.v2;

        // build oob data frame
        Frame oobFrame = df.mapRows(oobIndexes);

        // build accuracy on oob data frame
        ClassifierResult fit = c.predict(oobFrame);
        double refScore = Confusion.from(
                        oobFrame.rvar(firstTargetName()),
                        fit.firstClasses())
                .acceptedCases();

        // now for each input variable do computation
        for (String varName : inputNames()) {

            // shuffle values from variable
            Var shuffled = oobFrame.rvar(varName).fapply(VShuffle.filter());

            // build oob frame with shuffled variable
            Frame oobReduced = oobFrame.removeVars(VarRange.of(varName)).bindVars(shuffled);

            // compute accuracy on oob shuffled frame

            ClassifierResult pfit = c.predict(oobReduced);
            double acc = Confusion.from(
                    oobReduced.rvar(firstTargetName()),
                    pfit.firstClasses()
            ).acceptedCases();

            if (!permVIMap.containsKey(varName)) {
                permVIMap.put(varName, new ArrayList<>());
            }
            permVIMap.get(varName).add(refScore - acc);
        }
    }

    private void gainVICompute(Pair<ClassifierModel<?, ?, ?>, Mapping> weak) {
        CTree weakTree = (CTree) weak.v1;
        var scores = DensityVector.emptyByLabels(true, inputNames());
        collectGainVI(weakTree.getRoot(), scores);
        for (int j = 0; j < inputNames().length; j++) {
            String varName = inputName(j);
            if (!gainVIMap.containsKey(varName)) {
                gainVIMap.put(varName, new ArrayList<>());
            }
            gainVIMap.get(varName).add(scores.get(varName));
        }
    }

    private void collectGainVI(Node node, DensityVector<String> dv) {
        if (node.leaf) {
            return;
        }
        String varName = node.bestCandidate.testName();
        double score = Math.abs(node.bestCandidate.score());
        dv.increment(varName, score * node.density.sum());
        node.children.forEach(child -> collectGainVI(child, dv));
    }

    private void freqVICompute(Pair<ClassifierModel<?, ?, ?>, Mapping> pair) {
        CTree weak = (CTree) pair.v1;
        var scores = DensityVector.emptyByLabels(true, inputNames());
        collectFreqVI(weak.getRoot(), scores);
        for (String varName : inputNames) {
            freqVIMap.computeIfAbsent(varName, name -> new ArrayList<>()).add(scores.get(varName));
        }
    }

    private void collectFreqVI(Node node, DensityVector<String> dv) {
        if (node.leaf) {
            return;
        }
        String varName = node.bestCandidate.testName();
        dv.increment(varName, node.density.sum());
        node.children.forEach(child -> collectFreqVI(child, dv));
    }

    private void oobCompute(Frame df, Pair<ClassifierModel<?, ?, ?>, Mapping> pair) {
        double totalOobError;
        double totalOobInstances;
        Mapping oobMap = pair.v2;
        Frame oobTest = df.mapRows(oobMap);

        var fit = pair.v1.predict(oobTest);
        for (int j = 0; j < oobTest.rowCount(); j++) {
            int fitIndex = fit.firstClasses().getInt(j);
            oobDensities.inc(oobMap.get(j), fitIndex - 1, 1.0);
        }
        oobPredictedClasses.clearRows();
        totalOobError = 0.0;
        totalOobInstances = 0.0;

        int[] indexes = oobDensities.argmax(1);
        for (int i = 0; i < indexes.length; i++) {
            String bestLevel = firstTargetLevels().get(indexes[i] + 1);
            oobPredictedClasses.setLabel(i, bestLevel);
            if (!bestLevel.equals(oobTrueClass.getLabel(i))) {
                totalOobError++;
            }
            totalOobInstances++;
        }
        oobError = (totalOobInstances > 0) ? totalOobError / totalOobInstances : 0.0;
    }

    private Pair<ClassifierModel<?, ?, ?>, Mapping> buildWeakPredictor(Frame df, Var weights) {
        var weak = model.get().newInstance();
        RowSampler.Sample sample = rowSampler.get().nextSample(df, weights);
        weak.fit(sample.df(), sample.weights(), firstTargetName());
        return Pair.from(weak, sample.getComplementMapping());
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDensities) {
        ClassifierResult cp = ClassifierResult.build(this, df, true, true);
        List<ClassifierResult> predictions = new ArrayList<>();
        for (var predictor : predictors) {
            predictions.add(predictor.predict(df,
                    baggingMode.get().isUseClass(),
                    baggingMode.get().isUseDensities()));
        }
        baggingMode.get().computeDensity(firstTargetLevels(), predictions, cp.firstClasses(), cp.firstDensity());
        return cp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("; fitted:").append(hasLearned());
        if (hasLearned()) {
            sb.append(", fitted trees:").append(predictors.size());
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("CForest\n");
        sb.append("=======\n\n");

        sb.append("Description:\n");
        sb.append(fullName()).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");
        sb.append("Model fitted: ").append(hasLearned()).append(".\n");

        if (!hasLearned()) {
            return sb.toString();
        }

        sb.append("Learned model:\n");
        sb.append(inputVarsSummary(printer, options));
        sb.append(targetVarsSummary());
        sb.append("\nFitted trees:").append(predictors.size()).append("\n");
        sb.append("oob enabled:").append(oob.get()).append("\n");
        if (oob.get()) {
            sb.append("oob error:").append(Format.floatFlex(oobError)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary();
    }

    @Override
    public String toFullContent(POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(toSummary()).append("\n");
        if (hasLearned() && viFreq.get()) {
            sb.append("Frequency Variable Importance:\n");
            sb.append(getFreqVIInfo().toFullContent(options)).append("\n");
        }
        if (hasLearned() && viGain.get()) {
            sb.append("Gain Variable Importance:\n");
            sb.append(getGainVIInfo().toFullContent(options)).append("\n");
        }
        if (hasLearned() && viPerm.get()) {
            sb.append("Permutation Variable Importance:\n");
            sb.append(getGainVIInfo().toFullContent(options)).append("\n");
        }
        return sb.toString();
    }
}
