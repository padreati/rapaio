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

package rapaio.ml.classifier.ensemble;

import rapaio.core.CoreTools;
import rapaio.core.distributions.Distribution;
import rapaio.core.tools.DVector;
import rapaio.data.*;
import rapaio.data.filter.FFilter;
import rapaio.data.filter.Filters;
import rapaio.data.sample.FrameSample;
import rapaio.data.sample.FrameSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.Confusion;
import rapaio.util.Pair;
import rapaio.util.Util;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends AbstractClassifier {

    private static final long serialVersionUID = -145958939373105497L;

    private boolean oobComp = false;
    private boolean freqVIComp = false;
    private boolean gainVIComp = false;
    private boolean permVIComp = false;

    private Classifier c = CTree.newCART();
    private BaggingMode baggingMode = BaggingMode.VOTING;

    // learning artifacts
    private double oobError = Double.NaN;
    private List<Classifier> predictors = new ArrayList<>();
    private Map<Integer, DVector> oobDensities;
    private Var oobFit;
    private Var oobTrueClass;
    private Map<String, List<Double>> freqVIMap = new HashMap<>();
    private Map<String, List<Double>> gainVIMap = new HashMap<>();
    private Map<String, List<Double>> permVIMap = new HashMap<>();

    private CForest() {
        withRuns(10);
        this.baggingMode = BaggingMode.VOTING;
        this.c = CTree.newCART().withVarSelector(VarSelector.AUTO);
        this.oobComp = false;
        this.withSampler(new FrameSampler.Bootstrap(1));
    }

    public static CForest newRF() {
        return new CForest();
    }

    @Override
    public String name() {
        return "CForest";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("{");
        sb.append("runs:").append(runs()).append(";");
        sb.append("baggingMode:").append(baggingMode.name()).append(";");
        sb.append("oob:").append(oobComp).append(";");
        sb.append("sampler:").append(sampler().name()).append(";");
        sb.append("tree:").append(c.fullName());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Classifier newInstance() {
        return new CForest()
                .withRuns(runs())
                .withBaggingMode(baggingMode)
                .withOobComp(oobComp)
                .withFreqVIComp(freqVIComp)
                .withGainVIComp(gainVIComp)
                .withPermVIComp(permVIComp)
                .withClassifier(c.newInstance())
                .withSampler(sampler());
    }

    public CForest withRuns(int runs) {
        return (CForest) super.withRuns(runs);
    }

    public CForest withFreqVIComp(boolean freqVIComp) {
        this.freqVIComp = freqVIComp;
        return this;
    }

    public CForest withGainVIComp(boolean gainVIComp) {
        this.gainVIComp = gainVIComp;
        return this;
    }

    public CForest withPermVIComp(boolean permVIComp) {
        this.permVIComp = permVIComp;
        return this;
    }

    public CForest withOobComp(boolean oobCompute) {
        this.oobComp = oobCompute;
        return this;
    }

    public CForest withBaggingMode(BaggingMode baggingMode) {
        this.baggingMode = baggingMode;
        return this;
    }

    public CForest withBootstrap() {
        return withSampler(new FrameSampler.Bootstrap(1));
    }

    public CForest withBootstrap(double p) {
        return withSampler(new FrameSampler.Bootstrap(p));
    }

    public CForest withNoSampling() {
        return withSampler(new FrameSampler.Identity());
    }

    @Override
    public CForest withSampler(FrameSampler sampler) {
        return (CForest) super.withSampler(sampler);
    }

    public CForest withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    public CForest withMCols(int mcols) {
        if (c instanceof CTree) {
            ((CTree) c).withMCols(mcols);
        }
        return this;
    }

    public CForest withVarSelector(VarSelector varSelector) {
        if (c instanceof CTree) {
            ((CTree) c).withVarSelector(varSelector);
        }
        return this;
    }


    @Override
    public Capabilities capabilities() {
        Capabilities cc = c.capabilities();
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
                .withInputCount(cc.getMinInputCount(), cc.getMaxInputCount())
                .withInputTypes(cc.getInputTypes().stream().toArray(VarType[]::new))
                .withAllowMissingInputValues(cc.getAllowMissingInputValues())
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL)
                .withAllowMissingTargetValues(false);
    }

    public List<Classifier> getClassifiers() {
        return predictors;
    }

    public double getOobError() {
        return oobError;
    }

    public Confusion getOobInfo() {
        return new Confusion(oobTrueClass, oobFit);
    }

    public Frame getFreqVIInfo() {
        Var name = Nominal.empty().withName("name");
        Var score = Numeric.empty().withName("score mean");
        Var sd = Numeric.empty().withName("score sd");
        for (Map.Entry<String, List<Double>> e : freqVIMap.entrySet()) {
            name.addLabel(e.getKey());
            Numeric scores = Numeric.copy(e.getValue());
            sd.addValue(CoreTools.var(scores).sdValue());
            score.addValue(CoreTools.mean(scores).value());
        }
        double maxScore = CoreTools.max(score).value();
        Var scaled = Numeric.from(score.rowCount(), row -> 100.0 * score.value(row) / maxScore).withName("scaled score");
        return Filters.refSort(SolidFrame.wrapOf(name, score, sd, scaled), score.refComparator(false)).solidCopy();
    }

    public Frame getGainVIInfo() {
        Var name = Nominal.empty().withName("name");
        Var score = Numeric.empty().withName("score mean");
        Var sd = Numeric.empty().withName("score sd");
        for (Map.Entry<String, List<Double>> e : gainVIMap.entrySet()) {
            name.addLabel(e.getKey());
            Numeric scores = Numeric.copy(e.getValue());
            sd.addValue(CoreTools.var(scores).sdValue());
            score.addValue(CoreTools.mean(scores).value());
        }
        double maxScore = CoreTools.max(score).value();
        Var scaled = Numeric.from(score.rowCount(), row -> 100.0 * score.value(row) / maxScore).withName("scaled score");
        return Filters.refSort(SolidFrame.wrapOf(name, score, sd, scaled), score.refComparator(false)).solidCopy();
    }

    public Frame getPermVIInfo() {
        Var name = Nominal.empty().withName("name");
        Var score = Numeric.empty().withName("score mean");
        Var sds = Numeric.empty().withName("score sd");
        Var zscores = Numeric.empty().withName("z-score");
        Var pvalues = Numeric.empty().withName("p-value");
        Distribution normal = CoreTools.distNormal();
        for (Map.Entry<String, List<Double>> e : permVIMap.entrySet()) {
            name.addLabel(e.getKey());
            Numeric scores = Numeric.copy(e.getValue());
            double mean = CoreTools.mean(scores).value();
            double sd = CoreTools.var(scores).sdValue();
            double zscore = mean / (sd);
            double pvalue = normal.cdf(2 * normal.cdf(-Math.abs(zscore)));
            score.addValue(Math.abs(mean));
            sds.addValue(sd);
            zscores.addValue(Math.abs(zscore));
            pvalues.addValue(pvalue);
        }
        return Filters.refSort(SolidFrame.wrapOf(name, score, sds, zscores, pvalues), zscores.refComparator(false)).solidCopy();
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        double totalOobInstances = 0;
        double totalOobError = 0;
        if (oobComp) {
            oobDensities = new HashMap<>();
            oobTrueClass = df.var(firstTargetName()).solidCopy();
            oobFit = Nominal.empty(df.rowCount(), firstTargetLevels());
            for (int i = 0; i < df.rowCount(); i++) {
                oobDensities.put(i, DVector.newEmpty(false, firstTargetLevels()));
            }
        }
        if (freqVIComp && c instanceof CTree) {
            freqVIMap.clear();
        }
        if (gainVIComp && c instanceof CTree) {
            gainVIMap.clear();
        }
        if (permVIComp) {
            permVIMap.clear();
        }

        if (runPoolSize() == 0) {
            predictors = new ArrayList<>();
            for (int i = 0; i < runs(); i++) {
                Pair<Classifier, List<Integer>> weak = buildWeakPredictor(df, weights);
                predictors.add(weak._1);
                if (oobComp) {
                    oobCompute(df, weak);
                }
                if (freqVIComp && c instanceof CTree) {
                    freqVICompute(weak);
                }
                if (gainVIComp && c instanceof CTree) {
                    gainVICompute(weak);
                }
                if (permVIComp) {
                    permVICompute(df, weak);
                }
                if (runningHook() != null) {
                    runningHook().accept(this, i + 1);
                }
            }
        } else {
            // build in parallel the trees, than oob and running hook cannot run at the
            // same moment when weak tree was built
            // for a real running hook behavior run without threading
            predictors = new ArrayList<>();
            List<Pair<Classifier, List<Integer>>> list = Util.rangeStream(runs(), runPoolSize() > 0).boxed()
                    .map(s -> buildWeakPredictor(df, weights))
                    .collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                Pair<Classifier, List<Integer>> weak = list.get(i);
                predictors.add(weak._1);
                if (oobComp) {
                    oobCompute(df, weak);
                }
                if (freqVIComp && c instanceof CTree) {
                    freqVICompute(weak);
                }
                if (gainVIComp && c instanceof CTree) {
                    gainVICompute(weak);
                }
                if (permVIComp) {
                    permVICompute(df, weak);
                }
                if (runningHook() != null) {
                    runningHook().accept(this, i + 1);
                }
            }
        }
        return true;
    }

    private void permVICompute(Frame df, Pair<Classifier, List<Integer>> weak) {
        Classifier c = weak._1;
        List<Integer> oobIndexes = weak._2;

        // build oob data frame
        Frame oobFrame = df.mapRows(Mapping.wrap(oobIndexes));

        // build accuracy on oob data frame
        CFit fit = c.fit(oobFrame);
        double refScore = new Confusion(
                oobFrame.var(firstTargetName()),
                fit.firstClasses())
                .acceptedCases();

        // now for each input variable do computation
        for (String varName : inputNames()) {

            // shuffle values from variable
            Var shuffled = Filters.shuffle(oobFrame.var(varName));

            // build oob frame with shuffled variable
            Frame oobReduced = oobFrame.removeVars(varName).bindVars(shuffled);

            // compute accuracy on oob shuffled frame

            CFit pfit = c.fit(oobReduced);
            double acc = new Confusion(
                    oobReduced.var(firstTargetName()),
                    pfit.firstClasses()
            ).acceptedCases();

            if (!permVIMap.containsKey(varName)) {
                permVIMap.put(varName, new ArrayList<>());
            }
            permVIMap.get(varName).add(refScore - acc);
        }
    }

    private void gainVICompute(Pair<Classifier, List<Integer>> weak) {
        CTree weakTree = (CTree) weak._1;
        DVector scores = DVector.newEmpty(false, inputNames());
        collectGainVI(weakTree.getRoot(), scores);
        for (int j = 0; j < inputNames().length; j++) {
            String varName = inputName(j);
            double score = scores.get(varName);
            if (!gainVIMap.containsKey(varName)) {
                gainVIMap.put(varName, new ArrayList<>());
            }
            gainVIMap.get(varName).add(score);
        }
    }

    private void collectGainVI(CTree.Node node, DVector dv) {
        if (node.isLeaf())
            return;
        String varName = node.getBestCandidate().getTestName();
        double score = Math.abs(node.getBestCandidate().getScore());
        dv.increment(varName, score * node.getDensity().sum());
        node.getChildren().forEach(child -> collectGainVI(child, dv));
    }

    private void freqVICompute(Pair<Classifier, List<Integer>> weak) {
        CTree weakTree = (CTree) weak._1;
        DVector scores = DVector.newEmpty(false, inputNames());
        collectFreqVI(weakTree.getRoot(), scores);
        for (int j = 0; j < inputNames().length; j++) {
            String varName = inputName(j);
            double score = scores.get(varName);
            if (!freqVIMap.containsKey(varName)) {
                freqVIMap.put(varName, new ArrayList<>());
            }
            freqVIMap.get(varName).add(score);
        }
    }

    private void collectFreqVI(CTree.Node node, DVector dv) {
        if (node.isLeaf())
            return;
        String varName = node.getBestCandidate().getTestName();
        double score = Math.abs(node.getBestCandidate().getScore());
        dv.increment(varName, node.getDensity().sum());
        node.getChildren().forEach(child -> collectFreqVI(child, dv));
    }

    private void oobCompute(Frame df, Pair<Classifier, List<Integer>> weak) {
        double totalOobError;
        double totalOobInstances;
        List<Integer> oobIndexes = weak._2;
        Frame oobTest = df.mapRows(Mapping.wrap(oobIndexes));
        CFit fit = weak._1.fit(oobTest);
        for (int j = 0; j < oobTest.rowCount(); j++) {
            int fitIndex = fit.firstClasses().index(j);
            oobDensities.get(oobIndexes.get(j)).increment(fitIndex, 1.0);
        }
        oobFit.clear();
        totalOobError = 0.0;
        totalOobInstances = 0.0;
        for (Map.Entry<Integer, DVector> e : oobDensities.entrySet()) {
            if (e.getValue().sum() > 0) {
                int bestIndex = e.getValue().findBestIndex();
                String bestLevel = firstTargetLevels()[bestIndex];
                oobFit.setLabel(e.getKey(), bestLevel);
                if (!bestLevel.equals(oobTrueClass.label(e.getKey()))) {
                    totalOobError++;
                }
                totalOobInstances++;
            }
        }
        oobError = (totalOobInstances > 0) ? totalOobError / totalOobInstances : 0.0;
    }

    private Pair<Classifier, List<Integer>> buildWeakPredictor(Frame df, Var weights) {
        Classifier weak = c.newInstance();

        FrameSample sample = sampler().newSample(df, weights);

        Frame trainFrame = sample.df;
        Var trainWeights = sample.weights;

        weak.train(trainFrame, trainWeights, firstTargetName());
        List<Integer> oobIndexes = new ArrayList<>();
        if (oobComp) {
            Set<Integer> out = sample.mapping.rowStream().boxed().collect(toSet());
            oobIndexes = IntStream.range(0, df.rowCount()).filter(row -> !out.contains(row)).boxed().collect(toList());
        }
        return Pair.from(weak, oobIndexes);
    }

    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDensities) {
        CFit cp = CFit.build(this, df, true, true);
        List<CFit> treeFits = predictors.stream().parallel()
                .map(pred -> pred.fit(df, baggingMode.needsClass(), baggingMode.needsDensity()))
                .collect(Collectors.toList());
        baggingMode.computeDensity(firstTargetLevels(), new ArrayList<>(treeFits), cp.firstClasses(), cp.firstDensity());
        return cp;
    }

    @Override
    public CForest withRunningHook(BiConsumer<Classifier, Integer> runningHook) {
        return (CForest) super.withRunningHook(runningHook);
    }

    @Override
    public CForest withRunPoolSize(int poolSize) {
        return (CForest) super.withRunPoolSize(poolSize);
    }

    @Override
    public CForest withInputFilters(FFilter... filters) {
        return (CForest) super.withInputFilters(filters);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CForest model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName().replaceAll(";", ";\n")).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().summary()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append(baseSummary());

        // stuff specific to rf
        // todo


        return sb.toString();
    }
}
