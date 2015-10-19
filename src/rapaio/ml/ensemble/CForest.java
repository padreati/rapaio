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

package rapaio.ml.ensemble;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.sample.FrameSample;
import rapaio.data.sample.FrameSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends AbstractClassifier {

    private static final long serialVersionUID = -145958939373105497L;

    protected int runs = 0;
    protected boolean oobComp = false;
    protected Classifier c = CTree.newC45();
    protected BaggingMode baggingMode = BaggingMode.VOTING;
    //
    protected double totalOobInstances = 0;
    protected double totalOobError = 0;
    protected double oobError = Double.NaN;
    protected List<Classifier> predictors = new ArrayList<>();

    public CForest() {
        this.runs = 10;
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
        sb.append("CForest {");
        sb.append("runs:").append(runs).append(";");
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
                .withRuns(runs)
                .withBaggingMode(baggingMode)
                .withClassifier(c.newInstance())
                .withSampler(sampler());
    }

    public CForest withRuns(int runs) {
        this.runs = runs;
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

    public double getOobError() {
        return oobError;
    }

    @Override
    public CForest learn(Frame dfOld, Var weights, String... targetVarNames) {

        Frame df = prepareLearning(dfOld, weights, targetVarNames);

        totalOobInstances = 0;
        totalOobError = 0;

        predictors = Util.rangeStream(runs, poolSize() > 0).boxed()
                .map(s -> buildWeakPredictor(df, weights))
                .collect(Collectors.toList());
        if (oobComp) {
            oobError = totalOobError / totalOobInstances;
        }
        return this;
    }

    private Classifier buildWeakPredictor(Frame df, Var weights) {
        Classifier weak = c.newInstance();

        FrameSample sample = sampler().newSample(df, weights);

        Frame trainFrame = sample.df;
        Var trainWeights = sample.weights;
        Frame oobFrame = df.removeRows(sample.mapping);

        weak.learn(trainFrame, trainWeights, firstTargetName());
        if (oobComp) {
            // TODO This must be corrected, right now is wrong!@@@@@@
            CFit cp = weak.fit(oobFrame);
            double oobError = new ConfusionMatrix(oobFrame.var(firstTargetName()), cp.firstClasses()).errorCases();
            synchronized (this) {
                totalOobInstances += oobFrame.rowCount();
                totalOobError += oobError;
            }
        }
        return weak;
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
