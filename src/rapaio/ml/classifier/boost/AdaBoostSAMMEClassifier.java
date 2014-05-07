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

package rapaio.ml.classifier.boost;

import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.colselect.ColSelector;
import rapaio.ml.classifier.tree.TreeClassifier;
import rapaio.core.sample.DiscreteSampling;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMMEClassifier extends AbstractClassifier implements RunningClassifier {

    final double delta_error = 10e-10;
    // parameters

    Classifier base = new TreeClassifier()
            .withMaxDepth(2)
            .withNominalMethod(TreeClassifier.NominalMethods.BINARY)
            .withNumericMethod(TreeClassifier.NumericMethods.BINARY);
    int runs = 0;
    double sampling = 0;
    boolean stopOnError=false;

    // model artifacts

    List<Double> a;
    List<Classifier> h;
    Numeric w;
    double k;

    public AdaBoostSAMMEClassifier() {
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
    }

    @Override
    public AdaBoostSAMMEClassifier newInstance() {
        return (AdaBoostSAMMEClassifier) new AdaBoostSAMMEClassifier()
                .withClassifier(this.base)
                .withRuns(this.runs)
                .withColSelector(this.colSelector)
                .withStopOnError(stopOnError);
    }

    @Override
    public String name() {
        return "AdaBoost.SAMME";
    }

    @Override
    public String fullName() {
        if (sampling > 0) {
            return String.format("AdaBoost.SAMME (base: %s, runs: %d, sampling: true, sampling ratio: %.2f, stopOnError: %s)",
                    base.fullName(), runs, sampling, String.valueOf(stopOnError));
        }
        return String.format("AdaBoost.SAMME (base: %s, runs: %d, sampling: false, stopOnError: %s)",
                base.fullName(), runs, String.valueOf(stopOnError));
    }

    public AdaBoostSAMMEClassifier withColSelector(ColSelector colSelector) {
        this.colSelector = colSelector;
        return this;
    }

    public AdaBoostSAMMEClassifier withClassifier(Classifier weak) {
        this.base = weak;
        return this;
    }

    public AdaBoostSAMMEClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public AdaBoostSAMMEClassifier withSampling(double ratio) {
        this.sampling = ratio;
        return this;
    }

    public AdaBoostSAMMEClassifier withStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
        return this;
    }

    private int[] getSamplingRows(Frame df) {
        if (sampling > 0.0) {
            return new DiscreteSampling().sampleWR((int) (df.rowCount() * sampling), df.rowCount());
        }
        int[] rows = new int[df.rowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        return rows;
    }

    @Override
    public void learn(Frame df, String targetCol) {
        this.targetCol = targetCol;
        dict = df.col(targetCol).getDictionary();
        k = dict.length - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = df.getWeights().solidCopy();

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transformValue(x -> x / total);

        for (int i = 0; i < runs; i++) {
            boolean success = learnRound(df);
            if(!success && stopOnError) {
                break;
            }
        }
    }

    @Override
    public void learnFurther(Frame df, String targetColName, int additionalRuns) {

        if (w != null && targetCol != null && dict != null) {
            // if prev trained on something else than we have a problem
            if ((!targetColName.equals(targetCol) ||
                    k != df.col(targetColName).getDictionary().length - 1)) {
                throw new IllegalArgumentException("previous classifier trained on different target");
            }
            runs += additionalRuns;
        } else {
            runs = additionalRuns;
            learn(df, targetColName);
            return;
        }

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transformValue(x -> x / total);

        for (int i = h.size(); i < runs; i++) {
            boolean success = learnRound(df);
            if(!success && stopOnError) {
                break;
            }
        }
    }

    private boolean learnRound(Frame df) {
        int[] rows = getSamplingRows(df);
        Mapping mapping = new Mapping();
        for (int row : rows) mapping.add(df.rowId(row));
        Frame dfTrain = new MappedFrame(df.source(), mapping);
        for (int j = 0; j < rows.length; j++) {
            dfTrain.setWeight(j, w.getValue(rows[j]));
        }

        Classifier hh = base.newInstance();
        hh.learn(dfTrain, targetCol);
        hh.predict(df);
        double err = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (hh.pred().getIndex(j) != df.col(targetCol).getIndex(j)) {
                err += w.getValue(j);
            }
        }
        double alpha = Math.log((1. - err) / err) + Math.log(k - 1);
        if (err == 0) {
            if (h.isEmpty()) {
                h.add(hh);
                a.add(alpha);
            }
            System.out.println("Stop learning weak classifier. Computed err: 0");
            return false;
        }
        if (stopOnError &&  err > (1.0 - 1.0 / k) + delta_error) {
            System.out.println("Warning computed err: " + err
                    + ", required threshold: " + (1.0 - 1.0 / k) + delta_error);
            return false;
        }
        h.add(hh);
        a.add(alpha);

        for (int j = 0; j < w.rowCount(); j++) {
            if (hh.pred().getIndex(j) != df.col(targetCol).getIndex(j)) {
                w.setValue(j, w.getValue(j) * Math.exp(alpha));
            }
        }
        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transformValue(x -> x / total);

        return true;
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < h.size(); i++) {
            h.get(i).predict(df);
            for (int j = 0; j < df.rowCount(); j++) {
                int index = h.get(i).pred().getIndex(j);
                dist.setValue(j, index, dist.getValue(j, index) + a.get(i));
            }
        }

        // simply predict
        for (int i = 0; i < dist.rowCount(); i++) {

            double max = 0;
            int prediction = 0;
            for (int j = 1; j < dist.colCount(); j++) {
                if (dist.getValue(i, j) > max) {
                    prediction = j;
                    max = dist.getValue(i, j);
                }
            }
            pred.setIndex(i, prediction);
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");

    }
}
