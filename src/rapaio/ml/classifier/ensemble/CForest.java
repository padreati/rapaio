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

import rapaio.core.tools.DVector;
import rapaio.data.*;
import rapaio.data.sample.FrameSample;
import rapaio.data.sample.FrameSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.util.Pair;
import rapaio.util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends AbstractClassifier {

    private static final long serialVersionUID = -145958939373105497L;

    private boolean oobComp = false;
    private Classifier c = CTree.newCART();
    private BaggingMode baggingMode = BaggingMode.VOTING;
    private double oobError = Double.NaN;
    private List<Classifier> predictors = new ArrayList<>();
    private Map<Integer, DVector> oobDensities;
    private Var oobFit;
    private Var oobTrueClass;

    public CForest() {
        withRuns(10);
        this.baggingMode = BaggingMode.VOTING;
        this.c = CTree.newCART().withVarSelector(VarSelector.AUTO);
        this.oobComp = false;
        this.withSampler(new FrameSampler.Bootstrap(1));
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
                .withClassifier(c.newInstance())
                .withSampler(sampler());
    }

    public CForest withRuns(int runs) {
        return (CForest) super.withRuns(runs);
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

    public double getOobError() {
        return oobError;
    }

    @Override
    public CForest learn(Frame dfOld, Var weights, String... targetVarNames) {

        Frame df = prepareLearning(dfOld, weights, targetVarNames);

        double totalOobInstances = 0;
        double totalOobError = 0;
        if (oobComp) {
            oobDensities = new HashMap<>();
            oobTrueClass = df.var(firstTargetName()).solidCopy();
            oobFit = Nominal.newEmpty(df.rowCount(), firstTargetLevels());
            for (int i = 0; i < df.rowCount(); i++) {
                oobDensities.put(i, DVector.newEmpty(firstTargetLevels()));
            }
        }

        if (poolSize() == 0) {
            predictors = new ArrayList<>();
            for (int i = 0; i < runs(); i++) {
                Pair<Classifier, List<Integer>> weak = buildWeakPredictor(df, weights);
                predictors.add(weak._1);
                if (oobComp) {
                    oobCompute(df, weak);
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
            List<Pair<Classifier, List<Integer>>> list = Util.rangeStream(runs(), poolSize() > 0).boxed()
                    .map(s -> buildWeakPredictor(df, weights))
                    .collect(Collectors.toList());
            for (int i = 0; i < list.size(); i++) {
                Pair<Classifier, List<Integer>> weak = list.get(i);
                predictors.add(weak._1);
                if (oobComp) {
                    oobCompute(df, weak);
                }
                if (runningHook() != null) {
                    runningHook().accept(this, i + 1);
                }
            }
        }
        return this;
    }

    private void oobCompute(Frame df, Pair<Classifier, List<Integer>> weak) {
        double totalOobError;
        double totalOobInstances;
        List<Integer> oobIndexes = weak._2;
        Frame oobTest = df.mapRows(Mapping.newWrapOf(oobIndexes));
        CFit fit = weak._1.fit(oobTest);
        for (int j = 0; j < oobTest.rowCount(); j++) {
            int fitIndex = fit.firstClasses().index(j);
            oobDensities.get(oobIndexes.get(j)).increment(fitIndex, 1.0);
        }
        oobFit.clear();
        totalOobError = 0.0;
        totalOobInstances = 0.0;
        for (Map.Entry<Integer, DVector> e : oobDensities.entrySet()) {
            if (e.getValue().sum(false) > 0) {
                int bestIndex = e.getValue().findBestIndex(false);
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

        weak.learn(trainFrame, trainWeights, firstTargetName());
        List<Integer> oobIndexes = new ArrayList<>();
        if (oobComp) {
            Set<Integer> out = sample.mapping.rowStream().boxed().collect(toSet());
            oobIndexes = IntStream.range(0, df.rowCount()).filter(row -> !out.contains(row)).boxed().collect(toList());
        }
        return Pair.valueOf(weak, oobIndexes);
    }

    @Override
    public CFit fit(Frame dfOld, boolean withClasses, boolean withDensities) {

        Frame df = prepareFit(dfOld);
        CFit cp = CFit.newEmpty(this, df, true, true);
        cp.addTarget(firstTargetName(), firstTargetLevels());

        List<CFit> treeFits = predictors.stream().parallel()
                .map(pred -> pred.fit(df, baggingMode.needsClass(), baggingMode.needsDensity()))
                .collect(Collectors.toList());
        baggingMode.computeDensity(firstTargetLevels(), new ArrayList<>(treeFits), cp.firstClasses(), cp.firstDensity());
        return cp;
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
