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

import rapaio.core.sample.DiscreteSampling;
import rapaio.core.stat.ConfusionMatrix;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.colselect.ColSelector;
import rapaio.ml.classifier.colselect.RandomColSelector;
import rapaio.ml.classifier.tools.DensityVector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class ForestClassifier extends AbstractClassifier implements RunningClassifier {

    int runs = 0;
    boolean oobCompute = false;
    Classifier c = TreeClassifier.buildC45();
    double sampling = 1;
    BaggingMethod baggingMethod = BaggingMethods.DISTRIBUTION_SUM;
    //
    double totalOobInstances = 0;
    double totalOobError = 0;
    double oobError = Double.NaN;
    List<Classifier> predictors = new ArrayList<>();

    public static ForestClassifier buildRandomForest(int runs, int mcols, double sampling) {
        return new ForestClassifier()
                .withClassifier(TreeClassifier.buildCART())
                .withBaggingMethod(BaggingMethods.DISTRIBUTION_SUM)
                .withRuns(runs)
                .withColSelector(new RandomColSelector(mcols))
                .withSampling(sampling);
    }

    public static ForestClassifier buildRandomForest(int runs, int mcols, double sampling, Classifier c) {
        return new ForestClassifier()
                .withClassifier(c)
                .withBaggingMethod(BaggingMethods.DISTRIBUTION_SUM)
                .withRuns(runs)
                .withColSelector(new RandomColSelector(mcols))
                .withSampling(sampling);
    }


    @Override
    public Classifier newInstance() {
        return new ForestClassifier()
                .withColSelector(colSelector)
                .withRuns(runs)
                .withBaggingMethod(baggingMethod)
                .withClassifier(c);
    }

    @Override
    public String name() {
        return "ForestClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("(");
        sb.append("baggingMethod=").append(baggingMethod.name()).append(",");
        sb.append("colSelector=").append(colSelector.name()).append(",");
        sb.append("runs=").append(runs).append(",");
        sb.append("c=").append(c.fullName());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public ForestClassifier withColSelector(ColSelector colSelector) {
        this.colSelector = colSelector;
        return this;
    }

    public ForestClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }


    public ForestClassifier withOobError(boolean oobCompute) {
        this.oobCompute = oobCompute;
        return this;
    }

    public boolean getOobCompute() {
        return oobCompute;
    }

    public double getOobError() {
        return oobError;
    }

    public ForestClassifier withSampling(double sampling) {
        this.sampling = sampling;
        return this;
    }

    public double getSampling() {
        return sampling;
    }

    public BaggingMethod getBaggingMethod() {
        return baggingMethod;
    }

    public ForestClassifier withBaggingMethod(BaggingMethod baggingMethod) {
        this.baggingMethod = baggingMethod;
        return this;
    }

    public Classifier getClassifier() {
        return c;
    }

    public ForestClassifier withClassifier(Classifier c) {
        this.c = c;
        return this;
    }

    public List<Frame> produceSamples(Frame df) {
        List<Frame> frames = new ArrayList<>();
        if (sampling <= 0) {
            // no sampling
            frames.add(df.stream().toMappedFrame());
            frames.add(MappedFrame.newByRow(df));
            return frames;
        }

        Mapping train = Mapping.newEmpty();
        Mapping oob = Mapping.newEmpty();

        int[] sample = new DiscreteSampling().sampleWR((int) (df.rowCount() * sampling), df.rowCount());
        HashSet<Integer> rows = new HashSet<>();
        for (int row : sample) {
            rows.add(row);
            train.add(row);
        }
        for (int i = 0; i < df.rowCount(); i++) {
            if (rows.contains(i)) continue;
            oob.add(i);
        }

        frames.add(MappedFrame.newByRow(df, train));
        frames.add(MappedFrame.newByRow(df, oob));

        return frames;
    }

    @Override
    public void learn(Frame df, String targetCol) {

        this.targetCol = targetCol;
        this.dict = df.col(targetCol).dictionary();

        predictors.clear();

        totalOobInstances = 0;
        totalOobError = 0;

        for (int i = 0; i < runs; i++) {
            buildWeakPredictor(df);
        }

        if (oobCompute) {
            oobError = totalOobError / totalOobInstances;
        }
    }

    @Override
    public void learnFurther(Frame df, String targetName, int additionalRuns) {

        if (targetCol != null && dict != null) {
            this.runs += additionalRuns;
        } else {
            this.runs = additionalRuns;
            learn(df, targetName);
            return;
        }

        for (int i = predictors.size(); i < runs; i++) {
            buildWeakPredictor(df);
        }
    }

    private void buildWeakPredictor(Frame df) {
        Classifier pred = c.newInstance();
        pred.withColSelector(colSelector);

        List<Frame> samples = produceSamples(df);
        Frame train = samples.get(0);
        Frame oob = samples.get(1);

        pred.learn(train, targetCol);
        if (oobCompute) {
            pred.predict(oob);
            totalOobInstances += oob.rowCount();
            totalOobError += 1 - new ConfusionMatrix(oob.col(targetCol), pred.pred()).accuracy();
        }
        predictors.add(pred);
    }

    @Override
    public void predict(Frame df) {
        pred = Nominal.newEmpty(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        List<Frame> distributions = new ArrayList<>();
        predictors.forEach(p -> {
            p.predict(df);
            distributions.add(p.dist());
        });

        baggingMethod.computeDensity(dict, distributions, pred, dist);
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        throw new NotImplementedException();
    }

    // components

    public static interface BaggingMethod extends Serializable {

        String name();

        void computeDensity(String[] dictionary, List<Frame> distributions, Nominal pred, Frame dist);
    }

    public static enum BaggingMethods implements BaggingMethod {

        VOTING {
            @Override
            public void computeDensity(String[] dictionary, List<Frame> distributions, Nominal pred, Frame dist) {
                distributions.forEach(d -> {
                    for (int i = 0; i < d.rowCount(); i++) {
                        DensityVector dv = new DensityVector(dictionary);
                        for (int j = 0; j < dictionary.length; j++) {
                            dv.update(j, d.value(i, j));
                        }
                        int best = dv.findBestIndex();
                        dist.setValue(i, best, dist.value(i, best) + 1);
                    }
                });
                for (int i = 0; i < pred.rowCount(); i++) {
                    DensityVector dv = new DensityVector(dictionary);
                    for (int j = 0; j < dictionary.length; j++) {
                        dv.update(j, dist.value(i, j));
                    }
                    pred.setValue(i, dv.findBestIndex());
                }
            }
        },
        DISTRIBUTION_SUM {
            @Override
            public void computeDensity(String[] dictionary, List<Frame> distributions, Nominal pred, Frame dist) {
                distributions.forEach(d -> {
                    for (int i = 0; i < d.rowCount(); i++) {
                        for (int j = 0; j < dictionary.length; j++) {
                            dist.setValue(i, j, dist.value(i, j) + d.value(i, j));
                        }
                    }
                });
                for (int i = 0; i < pred.rowCount(); i++) {
                    DensityVector dv = new DensityVector(dictionary);
                    for (int j = 0; j < dictionary.length; j++) {
                        dv.update(j, dist.value(i, j));
                    }
                    pred.setValue(i, dv.findBestIndex());
                }
            }
        }
    }
}
