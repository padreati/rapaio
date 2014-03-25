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
import rapaio.classifier.FurtherClassifier;
import rapaio.classifier.tree.DecisionStumpClassifier;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Numeric;

import java.util.ArrayList;
import java.util.List;

import static rapaio.core.MathBase.log;
import static rapaio.core.MathBase.min;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMMEClassifier extends AbstractClassifier
        implements FurtherClassifier<AdaBoostSAMMEClassifier> {

    Classifier classifier = new DecisionStumpClassifier();
    int weakCount = 1;

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
                .withClassifier(this.classifier)
                .withWeakCount(this.weakCount);
    }

    @Override
    public String classifierName() {
        return "AdaBoost.SAMME";
    }

    @Override
    public String classifierInstance() {
        return String.format("AdaBoost.SAMME (classifier: %s, weakCount: %d)",
                classifier.classifierInstance(), weakCount);
    }

    public AdaBoostSAMMEClassifier withClassifier(Classifier weak) {
        this.classifier = weak;
        return this;
    }

    public AdaBoostSAMMEClassifier withWeakCount(int weakCount) {
        this.weakCount = weakCount;
        return this;
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

        for (int i = 0; i < weakCount; i++) {
            Classifier hh = classifier.newInstance();
            hh.learn(df, w.solidCopy(), targetCol);
            hh.predict(df);
            Nominal hpred = hh.pred();

            double err = 0;
            for (int j = 0; j < df.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetCol).getIndex(j)) {
                    err += w.getValue(j);
                }
            }
            double alpha = log((1. - err) / err) + log(k - 1);
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

            // update
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
    public void learnFurther(Frame df, Numeric weights, String targetColName, AdaBoostSAMMEClassifier prev) {

        // if is null than fallback on normal learning
        if (prev == null || prev.h.isEmpty() || prev.a.isEmpty()) {
            learn(df, weights, targetColName);
            return;
        }

        // should not be the same instance
        if (this.equals(prev)) {
            throw new IllegalArgumentException("Previous classifier should be different than current classifier");
        }

        // if prev trained on something else than we have a problem
        if (!targetColName.equals(prev.targetCol) ||
                !(prev.k != df.col(targetColName).getDictionary().length - 2)) {
            throw new IllegalArgumentException("previous classifier trained on different target");
        }

        dict = prev.dict;
        targetCol = prev.targetCol;
        k = prev.k;

        h = new ArrayList<>(prev.h);
        a = new ArrayList<>(prev.a);

        if (prev.w != null) {
            w = prev.w.solidCopy();
        } else if (weights != null) {
            w = weights.solidCopy();
        } else {
            w = new Numeric(df.rowCount());
            w.stream().transformValue(x -> 1.0);
        }


        for (int i = h.size(); i < weakCount; i++) {
            Classifier hh = classifier.newInstance();
            hh.learn(df, w.solidCopy(), targetColName);
            hh.predict(df);
            Nominal hpred = hh.pred();

            double err = 0;
            for (int j = 0; j < df.rowCount(); j++) {
                if (hpred.getIndex(j) != df.col(targetColName).getIndex(j)) {
                    err += w.getValue(j);
                }
            }
            double alpha = log((1. - err) / err) + log(k - 1);
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

            // update
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

        for (int i = 0; i < min(weakCount, h.size()); i++) {
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
        sb.append("> ").append(classifierInstance()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");

    }
}
