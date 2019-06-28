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

package rapaio.experiment.ml.classifier.ensemble;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.core.distributions.*;
import rapaio.core.stat.*;
import rapaio.core.tools.*;
import rapaio.data.*;
import rapaio.data.filter.frame.*;
import rapaio.data.filter.var.*;
import rapaio.data.sample.*;
import rapaio.experiment.ml.classifier.tree.*;
import rapaio.ml.classifier.*;
import rapaio.ml.common.*;
import rapaio.ml.eval.*;
import rapaio.printer.*;
import rapaio.util.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends AbstractClassifier implements DefaultPrintable {

    private static final long serialVersionUID = -145958939373105497L;

    private boolean oobComp;
    private boolean freqVIComp = false;
    private boolean gainVIComp = false;
    private boolean permVIComp = false;

    private Classifier c;
    private BaggingMode baggingMode;

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
        this.baggingMode = BaggingMode.DISTRIBUTION;
        this.c = CTree.newCART().withVarSelector(VarSelector.auto());
        this.oobComp = false;
        this.withSampler(RowSampler.bootstrap());
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
        return newInstanceDecoration(new CForest())
                .withBaggingMode(baggingMode)
                .withOobComp(oobComp)
                .withFreqVIComp(freqVIComp)
                .withGainVIComp(gainVIComp)
                .withPermVIComp(permVIComp)
                .withClassifier(c.newInstance());
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

    @Override
    public CForest withSampler(RowSampler sampler) {
        return (CForest) super.withSampler(sampler);
    }

    public CForest withClassifier(Classifier c) {
        this.c = c;
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
                .withInputCount(cc.minInputCount(), cc.maxInputCount())
                .withInputTypes(cc.inputTypes().toArray(new VType[0]))
                .withAllowMissingInputValues(cc.allowMissingInputValues())
                .withTargetCount(1, 1)
                .withTargetTypes(VType.NOMINAL)
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
        Var name = VarNominal.empty().withName("name");
        Var score = VarDouble.empty().withName("score mean");
        Var sd = VarDouble.empty().withName("score sd");
        for (Map.Entry<String, List<Double>> e : freqVIMap.entrySet()) {
            name.addLabel(e.getKey());
            VarDouble scores = VarDouble.copy(e.getValue());
            sd.addDouble(Variance.of(scores).sdValue());
            score.addDouble(Mean.of(scores).value());
        }
        double maxScore = Maximum.of(score).value();
        Var scaled = VarDouble.from(score.rowCount(), row -> 100.0 * score.getDouble(row) / maxScore).withName("scaled score");
        return SolidFrame.byVars(name, score, sd, scaled).fapply(FRefSort.by(score.refComparator(false))).solidCopy();
    }

    public Frame getGainVIInfo() {
        Var name = VarNominal.empty().withName("name");
        Var score = VarDouble.empty().withName("score mean");
        Var sd = VarDouble.empty().withName("score sd");
        for (Map.Entry<String, List<Double>> e : gainVIMap.entrySet()) {
            name.addLabel(e.getKey());
            VarDouble scores = VarDouble.copy(e.getValue());
            sd.addDouble(Variance.of(scores).sdValue());
            score.addDouble(Mean.of(scores).value());
        }
        double maxScore = Maximum.of(score).value();
        Var scaled = VarDouble.from(score.rowCount(), row -> 100.0 * score.getDouble(row) / maxScore).withName("scaled score");
        return SolidFrame.byVars(name, score, sd, scaled).fapply(FRefSort.by(score.refComparator(false))).solidCopy();
    }

    public Frame getPermVIInfo() {
        Var name = VarNominal.empty().withName("name");
        Var score = VarDouble.empty().withName("score mean");
        Var sds = VarDouble.empty().withName("score sd");
        Var zscores = VarDouble.empty().withName("z-score");
        Var pvalues = VarDouble.empty().withName("p-value");
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
        return SolidFrame.byVars(name, score, sds, zscores, pvalues).fapply(FRefSort.by(zscores.refComparator(false))).solidCopy();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        double totalOobInstances = 0;
        double totalOobError = 0;
        if (oobComp) {
            oobDensities = new HashMap<>();
            oobTrueClass = df.rvar(firstTargetName()).solidCopy();
            oobFit = VarNominal.empty(df.rowCount(), firstTargetLevels());
            for (int i = 0; i < df.rowCount(); i++) {
                oobDensities.put(i, DVector.empty(false, firstTargetLevels()));
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
                Pair<Classifier, IntList> weak = buildWeakPredictor(df, weights);
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
            IntStream intStream = IntStream.range(0, runs());
            if (runPoolSize() > 0) {
                intStream = intStream.parallel();
            }
            List<Pair<Classifier, IntList>> list = intStream
                    .boxed()
                    .map(s -> buildWeakPredictor(df, weights))
                    .collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                Pair<Classifier, IntList> weak = list.get(i);
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

    private void permVICompute(Frame df, Pair<Classifier, IntList> weak) {
        Classifier c = weak._1;
        IntList oobIndexes = weak._2;

        // build oob data frame
        Frame oobFrame = df.mapRows(Mapping.wrap(oobIndexes));

        // build accuracy on oob data frame
        CPrediction fit = c.predict(oobFrame);
        double refScore = new Confusion(
                oobFrame.rvar(firstTargetName()),
                fit.firstClasses())
                .acceptedCases();

        // now for each input variable do computation
        for (String varName : inputNames()) {

            // shuffle values from variable
            Var shuffled = oobFrame.rvar(varName).fapply(VShuffle.filter());

            // build oob frame with shuffled variable
            Frame oobReduced = oobFrame.removeVars(VRange.of(varName)).bindVars(shuffled);

            // compute accuracy on oob shuffled frame

            CPrediction pfit = c.predict(oobReduced);
            double acc = new Confusion(
                    oobReduced.rvar(firstTargetName()),
                    pfit.firstClasses()
            ).acceptedCases();

            if (!permVIMap.containsKey(varName)) {
                permVIMap.put(varName, new ArrayList<>());
            }
            permVIMap.get(varName).add(refScore - acc);
        }
    }

    private void gainVICompute(Pair<Classifier, IntList> weak) {
        CTree weakTree = (CTree) weak._1;
        DVector scores = DVector.empty(false, inputNames());
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

    private void collectGainVI(CTreeNode node, DVector dv) {
        if (node.isLeaf())
            return;
        String varName = node.getBestCandidate().getTestName();
        double score = Math.abs(node.getBestCandidate().getScore());
        dv.increment(varName, score * node.getDensity().sum());
        node.getChildren().forEach(child -> collectGainVI(child, dv));
    }

    private void freqVICompute(Pair<Classifier, IntList> weak) {
        CTree weakTree = (CTree) weak._1;
        DVector scores = DVector.empty(false, inputNames());
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

    private void collectFreqVI(CTreeNode node, DVector dv) {
        if (node.isLeaf())
            return;
        String varName = node.getBestCandidate().getTestName();
        double score = Math.abs(node.getBestCandidate().getScore());
        dv.increment(varName, node.getDensity().sum());
        node.getChildren().forEach(child -> collectFreqVI(child, dv));
    }

    private void oobCompute(Frame df, Pair<Classifier, IntList> weak) {
        double totalOobError;
        double totalOobInstances;
        IntList oobIndexes = weak._2;
        Frame oobTest = df.mapRows(Mapping.wrap(oobIndexes));
        CPrediction fit = weak._1.predict(oobTest);
        for (int j = 0; j < oobTest.rowCount(); j++) {
            int fitIndex = fit.firstClasses().getInt(j);
            oobDensities.get(oobIndexes.getInt(j)).increment(fitIndex, 1.0);
        }
        oobFit.clearRows();
        totalOobError = 0.0;
        totalOobInstances = 0.0;
        for (Map.Entry<Integer, DVector> e : oobDensities.entrySet()) {
            if (e.getValue().sum() > 0) {
                int bestIndex = e.getValue().findBestIndex();
                String bestLevel = firstTargetLevels().get(bestIndex);
                oobFit.setLabel(e.getKey(), bestLevel);
                if (!bestLevel.equals(oobTrueClass.getLabel(e.getKey()))) {
                    totalOobError++;
                }
                totalOobInstances++;
            }
        }
        oobError = (totalOobInstances > 0) ? totalOobError / totalOobInstances : 0.0;
    }

    private Pair<Classifier, IntList> buildWeakPredictor(Frame df, Var weights) {
        Classifier weak = c.newInstance();

        Sample sample = sampler().nextSample(df, weights);

        Frame trainFrame = sample.df;
        Var trainWeights = sample.weights;

        weak.fit(trainFrame, trainWeights, firstTargetName());
        IntList oobIndexes = new IntArrayList();
        if (oobComp) {
            Set<Integer> out = sample.mapping.stream().boxed().collect(toSet());
            oobIndexes = IntArrayList.wrap(IntStream.range(0, df.rowCount()).filter(row -> !out.contains(row)).toArray());
        }
        return Pair.from(weak, oobIndexes);
    }

    @Override
    protected CPrediction corePredict(Frame df, boolean withClasses, boolean withDensities) {
        CPrediction cp = CPrediction.build(this, df, true, true);
        List<CPrediction> treeFits = predictors.stream().parallel()
                .map(pred -> pred.predict(df, baggingMode.needsClass(), baggingMode.needsDensity()))
                .collect(Collectors.toList());
        baggingMode.computeDensity(firstTargetLevels(), new ArrayList<>(treeFits), cp.firstClasses(), cp.firstDensity());
        return cp;
    }

    @Override
    public CForest withRunningHook(BiConsumer<Classifier, Integer> runningHook) {
        return (CForest) super.withRunningHook(runningHook);
    }

    @Override
    public CForest withPoolSize(int poolSize) {
        return (CForest) super.withPoolSize(poolSize);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("CForest model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName().replaceAll(";", ";\n")).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");

        if (!hasLearned()) {
            return sb.toString();
        }

        sb.append("Learned model:\n");
        sb.append(baseSummary());

        // stuff specific to rf
        // todo


        return sb.toString();
    }
}
