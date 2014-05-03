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
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.colselect.ColSelector;
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
    //
    double oobError = Double.NaN;
    List<Classifier> predictors = new ArrayList<>();

    @Override
    public Classifier newInstance() {
        return new ForestClassifier()
                .withColSelector(colSelector)
                .withRuns(runs);
    }

    @Override
    public String name() {
        return "ForestClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("(");

        sb.append(")");
        return sb.toString();
    }

    @Override
    public ForestClassifier withColSelector(ColSelector colSelector) {
        super.withColSelector(colSelector);
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

    public List<Frame> produceSamples(Frame df) {
        List<Frame> frames = new ArrayList<>();
        if (sampling <= 0) {
            // no sampling
            frames.add(df.stream().toMappedFrame());
            frames.add(new MappedFrame(df.source(), new Mapping()));
            return frames;
        }

        Mapping train = new Mapping();
        Mapping oob = new Mapping();

        int[] sample = new DiscreteSampling().sampleWR((int) (df.rowCount() * sampling), df.rowCount());
        HashSet<Integer> rows = new HashSet<>();
        for (int row : sample) {
            rows.add(row);
            train.add(df.rowId(row));
        }
        for (int i = 0; i < df.rowCount(); i++) {
            if (rows.contains(i)) continue;
            oob.add(df.rowId(i));
        }

        frames.add(new MappedFrame(df.source(), train));
        frames.add(new MappedFrame(df.source(), oob));

        return frames;
    }

    @Override
    public void learn(Frame df, String targetCol) {

        this.targetCol = targetCol;
        this.dict = df.col(targetCol).getDictionary();

        predictors.clear();

        double totalOobInstances = 0;
        double totalOobError = 0;

        for (int i = 0; i < runs; i++) {
            Classifier pred = c.newInstance();
            pred.withColSelector(colSelector);

            List<Frame> samples = produceSamples(df);
            Frame train = samples.get(0);
            Frame oob = samples.get(1);

            pred.learn(train, targetCol);
            if (oobCompute) {
                pred.predict(oob);
                totalOobInstances += oob.rowCount();
                totalOobError += 1 - new ConfusionMatrix(oob.col(targetCol), pred.pred()).getAccuracy();
            }
            predictors.add(pred);
        }

        if (oobCompute) {
            oobError = totalOobError / totalOobInstances;
        }
    }

    @Override
    public void learnFurther(Frame df, String targetName, int runs) {
        throw new NotImplementedException();
    }

    @Override
    public void predict(Frame df) {
        throw new NotImplementedException();
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        throw new NotImplementedException();
    }

    // components

    public static interface BaggingMethod extends Serializable {

        String name();

        Frame computeDensity(String[] dictionary, List<Frame> densities);
    }

    public static enum BaggingMethods implements BaggingMethod {

        VOTING {
            @Override
            public Frame computeDensity(String[] dictionary, List<Frame> densities) {
                return null;
            }
        },
        SUM_ON_DISTRIBUTION {
            @Override
            public Frame computeDensity(String[] dictionary, List<Frame> densities) {
                return null;
            }
        }
    }
}
