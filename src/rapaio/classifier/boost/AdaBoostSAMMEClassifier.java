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

package rapaio.classifier.boost;

import rapaio.classifier.AbstractClassifier;
import rapaio.classifier.Classifier;
import rapaio.classifier.RunningClassifier;
import rapaio.classifier.tree.DecisionStumpClassifier;
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

    // parameters

    Classifier base = new DecisionStumpClassifier();
    int runs = 0;
    double sampling = 0;
    boolean bootstrap = false;

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
        return new AdaBoostSAMMEClassifier()
                .withClassifier(this.base)
                .withRuns(this.runs);
    }

    @Override
    public String name() {
        return "AdaBoost.SAMME";
    }

    @Override
    public String fullName() {
        if (sampling != 0) {
            return String.format("AdaBoost.SAMME (base: %s, runs: %d, sampling: true, sampling ratio: %.2f, bootstrap: %s)",
                    base.fullName(), runs, sampling, bootstrap);
        }
        return String.format("AdaBoost.SAMME (base: %s, runs: %d, sampling: false)",
                base.fullName(), runs);
    }

    public AdaBoostSAMMEClassifier withClassifier(Classifier weak) {
        this.base = weak;
        return this;
    }

    public AdaBoostSAMMEClassifier withRuns(int weakCount) {
        this.runs = weakCount;
        return this;
    }

    public AdaBoostSAMMEClassifier withSampling(double ratio, boolean bootstrap) {
        this.sampling = ratio;
        this.bootstrap = bootstrap;
        return this;
    }

    public AdaBoostSAMMEClassifier withNoSampling() {
        this.sampling = 0;
        return this;
    }

    private int[] getSamplingRows(Frame df) {
        if (sampling < 1.0) {
            if (bootstrap) {
                return new DiscreteSampling().sampleWR((int) (df.rowCount() * sampling), df.rowCount());
            } else {
                return new DiscreteSampling().sampleWOR((int) (df.rowCount() * sampling), df.rowCount());
            }
        }
        int[] rows = new int[df.rowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        return rows;
    }

    @Override
    public void learn(Frame df, Numeric weights, String targetCol) {
        this.targetCol = targetCol;
        dict = df.col(targetCol).getDictionary();
        k = dict.length - 1;

        if (weights != null) {
            w = weights.solidCopy();
        } else {
            w = new Numeric(df.rowCount());
            w.stream().transformValue(x -> 1.0);
        }

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transformValue(x -> x / total);

        for (int i = 0; i < runs; i++) {

            Frame dfTrain;
            Numeric wTrain;
            if (sampling == 0) {
                dfTrain = df;
                wTrain = w.solidCopy();
            } else {
                int[] rows = getSamplingRows(df);
                Mapping mapping = new Mapping();
                wTrain = new Numeric();
                for (int row : rows) {
                    mapping.add(df.rowId(row));
                    wTrain.addValue(w.getValue(row));
                }
                dfTrain = new MappedFrame(df.source(), mapping);
            }

            Classifier hh = base.newInstance();
            hh.learn(dfTrain, wTrain, targetCol);

            hh.predict(df);
            Nominal hpred = hh.pred();

            double err = 0;
            for (int j = 0; j < df.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetCol).getIndex(j)) {
                    err += w.getValue(j);
                }
            }
            double alpha = Math.log((1. - err) / err) + Math.log(k - 1);
            if (err == 0 || err > (1 - 1 / k)) {
                if (h.isEmpty()) {
                    h.add(hh);
                    a.add(alpha);
                }
                System.out.println("This should not be");
                break;
            }
            h.add(hh);
            a.add(alpha);

            // out
            for (int j = 0; j < w.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetCol).getIndex(j)) {
                    w.setValue(j, w.getValue(j) * (k - 1) / (k * err));
                } else {
                    w.setValue(j, w.getValue(j) / (k * (1. - err)));
                }
            }
        }
    }

    @Override
    public void learnFurther(Frame df, Numeric weights, String targetColName, int additionalRuns) {

        boolean prev = false;
        if (w != null && targetCol != null && dict != null) {
            prev = true;
        }

        // if prev trained on something else than we have a problem
        if (prev) {
            if ((!targetColName.equals(targetCol) ||
                    k != df.col(targetColName).getDictionary().length - 1)) {
                throw new IllegalArgumentException("previous classifier trained on different target");
            }
        }

        if (!prev) {
            targetCol = targetColName;
            dict = df.col(targetCol).getDictionary();
            k = dict.length - 1;

            h = new ArrayList<>();
            a = new ArrayList<>();
            runs = additionalRuns;
        } else {
            runs += additionalRuns;
        }

        if (w == null) {
            if (weights != null) {
                w = weights.solidCopy();
            } else {
                w = new Numeric(df.rowCount());
                w.stream().transformValue(x -> 1.0);
            }
        }

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transformValue(x -> x / total);

        for (int i = 0; i < additionalRuns; i++) {

            Frame dfTrain;
            Numeric wTrain;
            if (sampling == 0) {
                dfTrain = df;
                wTrain = w.solidCopy();
            } else {
                int[] rows = getSamplingRows(df);
                Mapping mapping = new Mapping();
                wTrain = new Numeric();
                for (int row : rows) {
                    mapping.add(df.rowId(row));
                    wTrain.addValue(w.getValue(row));
                }
                dfTrain = new MappedFrame(df.source(), mapping);
            }

            Classifier hh = base.newInstance();
            hh.learn(dfTrain, wTrain, targetCol);
            hh.predict(df);
            Nominal hpred = hh.pred();

            double err = 0;
            for (int j = 0; j < df.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetColName).getIndex(j)) {
                    err += w.getValue(j);
                }
            }
            double alpha = Math.log((1. - err) / err) + Math.log(k - 1);
            if (err == 0 || err > (1 - 1 / k)) {
                if (h.isEmpty()) {
                    h.add(hh);
                    a.add(alpha);
                }
                break;
            }
            if (err > (1 - 1 / k)) {
                i--;
                continue;
            }
            h.add(hh);
            a.add(alpha);

            // out
            for (int j = 0; j < w.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetColName).getIndex(j)) {
                    w.setValue(j, w.getValue(j) * (k - 1) / (k * err));
                } else {
                    w.setValue(j, w.getValue(j) / (k * (1. - err)));
                }
            }
        }
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < Math.min(runs, h.size()); i++) {
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
