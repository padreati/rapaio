/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier.tree;

import rapaio.core.sample.Sample;
import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Nominal;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.tools.DensityVector;
import rapaio.ml.classifier.tree.ctree.CTree;
import rapaio.ml.common.VarSelector;
import rapaio.ml.eval.ConfusionMatrix;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CForest extends AbstractClassifier implements RunningClassifier {

    int runs = 0;
    boolean oobCompute = false;
    Classifier c = CTree.newC45();
    Sampler sampler = new Sampler.Bootstrap(1.0);
    BaggingMethod baggingMethod = BaggingMethods.VOTING;
    //
    double totalOobInstances = 0;
    double totalOobError = 0;
    double oobError = Double.NaN;
    List<Classifier> predictors = new ArrayList<>();

    public static CForest newRF(int runs, int mcols, Sampler sampler) {
        return new CForest()
                .withClassifier(CTree.newCART().withVarSelector(new VarSelector.Random(mcols)))
                .withBaggingMethod(BaggingMethods.VOTING)
                .withRuns(runs)
                .withSampler(sampler);
    }

    public static CForest newRF(int runs, Sampler sampler) {
        return new CForest()
                .withClassifier(CTree.newCART().withVarSelector(new VarSelector.Random()))
                .withBaggingMethod(BaggingMethods.VOTING)
                .withRuns(runs)
                .withSampler(sampler);
    }

    @Override
    public Classifier newInstance() {
        return new CForest()
                .withRuns(runs)
                .withBaggingMethod(baggingMethod)
                .withSampler(sampler())
                .withOobError(oobCompute)
                .withClassifier(c);
    }

    @Override
    public String name() {
        return "CForest";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("baggingMethod=").append(baggingMethod.name()).append(",");
        sb.append("runs=").append(runs).append(",");
        sb.append("c=").append(c.fullName());
        sb.append("}");
        return sb.toString();
    }

    public CForest withRuns(int runs) {
        this.runs = runs;
        return this;
    }


    public CForest withOobError(boolean oobCompute) {
        this.oobCompute = oobCompute;
        return this;
    }

    public boolean getOobCompute() {
        return oobCompute;
    }

    public double getOobError() {
        return oobError;
    }

    public CForest withSampler(Sampler sampler) {
        this.sampler = sampler;
        return this;
    }

    public BaggingMethod getBaggingMethod() {
        return baggingMethod;
    }

    public CForest withBaggingMethod(BaggingMethod baggingMethod) {
        this.baggingMethod = baggingMethod;
        return this;
    }

    public Classifier getClassifier() {
        return c;
    }

    public CForest withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

        prepareLearning(df, weights, targetVarNames);
        if (targetNames().length != 1) {
            throw new IllegalArgumentException("Forest classifiers can learn only one target variable");
        }

        predictors.clear();

        totalOobInstances = 0;
        totalOobError = 0;

        IntStream.range(0, runs).parallel().forEach(s -> buildWeakPredictor(df, weights));

        if (oobCompute) {
            oobError = totalOobError / totalOobInstances;
        }
    }

    @Override
    public void learnFurther(Frame df, Var weights, String targetVars, int additionalRuns) {

        if (this.targetNames() != null && dictionaries() != null) {
            this.runs += additionalRuns;
        } else {
            this.runs = additionalRuns;
            learn(df, targetVars);
            return;
        }
        IntStream.range(0, runs).parallel().forEach(s -> buildWeakPredictor(df, weights));
    }

    private void buildWeakPredictor(Frame df, Var weights) {
        Classifier weak = c.newInstance();

        Sample sample = sampler.newSample(df, weights);

        Frame trainFrame = sample.df;
        Var trainWeights = sample.weights;
        Frame oobFrame = df.removeRows(sample.mapping);

        weak.learn(trainFrame, trainWeights, firstTargetName());
        if (oobCompute) {
            CResult cp = weak.predict(oobFrame);
            double oobError = new ConfusionMatrix(oobFrame.var(firstTargetName()), cp.firstClasses()).errorCases();
            synchronized (this) {
                totalOobInstances += oobFrame.rowCount();
                totalOobError += oobError;
            }
        }
        synchronized (this) {
            predictors.add(weak);
        }
    }

    @Override
    public CResult predict(Frame df, boolean withClasses, boolean withDensities) {
        CResult cp = CResult.newEmpty(this, df, true, true);
        cp.addTarget(firstTargetName(), firstDict());

        List<Frame> treeDensities = new ArrayList<>();
        predictors.forEach(p -> {
            CResult cpTree = p.predict(df, true, true);
            treeDensities.add(cpTree.firstDensity());
        });

        baggingMethod.computeDensity(firstDict(), treeDensities, cp.firstClasses(), cp.firstDensity());
        return cp;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        throw new NotImplementedException();
    }

    // components

    public static interface BaggingMethod extends Serializable {

        String name();

        void computeDensity(String[] dictionary, List<Frame> treeDensities, Nominal classes, Frame densities);
    }

    public static enum BaggingMethods implements BaggingMethod {

        VOTING {
            @Override
            public void computeDensity(String[] dictionary, List<Frame> treeDensities, Nominal classes, Frame densities) {
                treeDensities.forEach(d -> {
                    for (int i = 0; i < d.rowCount(); i++) {
                        DensityVector dv = new DensityVector(dictionary);
                        for (int j = 0; j < dictionary.length; j++) {
                            dv.update(j, d.value(i, j));
                        }
                        int best = dv.findBestIndex();
                        densities.setValue(i, best, densities.value(i, best) + 1);
                    }
                });
                for (int i = 0; i < classes.rowCount(); i++) {
                    DensityVector dv = new DensityVector(dictionary);
                    for (int j = 0; j < dictionary.length; j++) {
                        dv.update(j, densities.value(i, j));
                    }
                    dv.normalize(false);
                    for (int j = 0; j < dictionary.length; j++) {
                        densities.setValue(i, j, dv.get(j));
                    }
                    classes.setValue(i, dv.findBestIndex());
                }
            }
        },
        DISTRIBUTION {
            @Override
            public void computeDensity(String[] dictionary, List<Frame> treeDensities, Nominal classes, Frame densities) {
                for (int i = 0; i < densities.rowCount(); i++) {
                    for (int j = 0; j < densities.varCount(); j++) {
                        densities.setValue(i, j, 0);
                    }
                }
                treeDensities.forEach(d -> {
                    for (int i = 0; i < densities.rowCount(); i++) {
                        double t = 0.0;
                        for (int j = 0; j < densities.varCount(); j++) {
                            t += d.value(i, j);
                        }
                        for (int j = 0; j < densities.varCount(); j++) {
                            densities.setValue(i, j, densities.value(i, j) + d.value(i, j) / t);
                        }
                    }
                });
                for (int i = 0; i < classes.rowCount(); i++) {
                    DensityVector dv = new DensityVector(dictionary);
                    for (int j = 0; j < dictionary.length; j++) {
                        dv.update(j, densities.value(i, j));
                    }
                    dv.normalize(false);
                    for (int j = 0; j < dictionary.length; j++) {
                        densities.setValue(i, j, dv.get(j));
                    }
                    classes.setValue(i, dv.findBestIndex());
                }
            }
        }
    }
}
