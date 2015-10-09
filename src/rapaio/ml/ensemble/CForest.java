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
import rapaio.util.func.SFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Breiman random forest implementation.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class CForest extends AbstractClassifier {

    private static final long serialVersionUID = -145958939373105497L;

    protected int runs = 0;
    protected int topRuns = Integer.MAX_VALUE;
    protected boolean oobComp = false;
    protected Classifier c = CTree.newC45();
    protected SFunction<Classifier, Double> topSelector;
    protected BaggingMode baggingMode = BaggingMode.VOTING;
    //
    protected double totalOobInstances = 0;
    protected double totalOobError = 0;
    protected double oobError = Double.NaN;
    protected List<Classifier> predictors = new ArrayList<>();
    protected List<Double> predictorScores = new ArrayList<>();

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
        sb.append(" {");
        sb.append("runs:").append(runs).append(",");
        sb.append("baggingMode:").append(baggingMode.name()).append(",");
        sb.append("oob:").append(oobComp).append(",");
        sb.append("sampler:").append(sampler().name()).append(",");
        sb.append("tree:").append(c.fullName()).append("");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Classifier newInstance() {
        return new CForest()
                .withRuns(runs)
                .withBaggingMode(baggingMode)
                .withClassifier((CTree) c.newInstance())
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

    public CForest withClassifier(CTree c) {
        this.c = c;
        return this;
    }

    public CForest withMCols() {
        if (c instanceof CTree) {
            ((CTree) c).withMCols();
        }
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

    public CForest withTopSelector(int topRuns, SFunction<Classifier, Double> topSelector) {
        this.topSelector = topSelector;
        return this;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
                .withInputCount(1, 1_000_000)
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NOMINAL, VarType.ORDINAL, VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL)
                .withAllowMissingTargetValues(false);
    }

    public double getOobError() {
        return oobError;
    }

    @Override
    public CForest learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);
        if (targetNames().length != 1) {
            throw new IllegalArgumentException("Forest classifiers can learn only one target variable");
        }

        predictors.clear();

        totalOobInstances = 0;
        totalOobError = 0;

        IntStream.range(0, runs).parallel().forEach(s -> buildWeakPredictor(df, weights));

        if (oobComp) {
            oobError = totalOobError / totalOobInstances;
        }
        return this;
    }

    private void buildWeakPredictor(Frame df, Var weights) {
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
        if (topRuns >= runs) {
            synchronized (this) {
                predictors.add(weak);
            }
        } else {
            synchronized (this) {
                double score = topSelector.apply(weak);
                if (predictors.size() < topRuns) {
                    predictors.add(weak);
                    predictorScores.add(score);
                } else {
                    int minIndex = -1;
                    for (int i = 0; i < predictors.size(); i++) {
                        if (predictorScores.get(i) < score) {
                            if (minIndex == -1 || predictorScores.get(i) < predictorScores.get(minIndex)) {
                                minIndex = i;
                            }
                        }
                    }
                    if (minIndex != -1) {
                        predictors.set(minIndex, weak);
                        predictorScores.set(minIndex, score);
                    }
                }
            }
        }
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDensities) {
        CFit cp = CFit.newEmpty(this, df, true, true);
        cp.addTarget(firstTargetName(), firstDict());

        List<Frame> treeDensities = new ArrayList<>();
        predictors.stream().parallel()
                .map(pred -> pred.fit(df, true, true).firstDensity())
                .forEach(frame -> {
                    synchronized (treeDensities) {
                        treeDensities.add(frame);
                    }
                });
        baggingMode.computeDensity(firstDict(), new ArrayList<>(treeDensities), cp.firstClasses(), cp.firstDensity());
        return cp;
    }

}
