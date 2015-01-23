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

import rapaio.core.sample.Sampling;
import rapaio.data.*;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.tree.ctree.CTree;
import rapaio.ml.common.VarSelector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMMEClassifier extends AbstractClassifier implements RunningClassifier {

    final double delta_error = 10e-10;

    // parameters

    private Classifier base = CTree.newDecisionStump();
    private int runs = 0;
    private double sampling = 0;
    private boolean stopOnError = false;

    // model artifacts

    private List<Double> a;
    private List<Classifier> h;
    private Var w;
    private double k;

    public AdaBoostSAMMEClassifier() {
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
    }

    @Override
    public AdaBoostSAMMEClassifier newInstance() {
        return new AdaBoostSAMMEClassifier()
                .withClassifier(this.base.newInstance())
                .withRuns(this.runs)
                .withVarSelector(this.varSelector)
                .withStopOnError(stopOnError)
                .withSampling(sampling);
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

    public AdaBoostSAMMEClassifier withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
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
            return Sampling.sampleWR((int) (df.rowCount() * sampling), df.rowCount());
        }
        int[] rows = new int[df.rowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        return rows;
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVars) {
        List<String> targetVarList = new VarRange(targetVars).parseVarNames(df);
        if (targetVarList.isEmpty()) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetVarList.size() > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }
        this.targetNames = targetVarList.toArray(new String[targetVarList.size()]);
        dict = Arrays.stream(this.targetNames).collect(Collectors.toMap(s -> s, s -> df.var(s).dictionary()));

        k = firstDictionary().length - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = weights.solidCopy();

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transValue(x -> x / total);

        for (int i = 0; i < runs; i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
        }
    }

    @Override
    public void learnFurther(Frame df, Var weights, String targetVarsRange, int additionalRuns) {

        List<String> targetVarList = new VarRange(targetVarsRange).parseVarNames(df);
        String[] targetVars = targetVarList.toArray(new String[targetVarList.size()]);

        if (w != null && this.targetNames != null && dict != null) {
            // if prev trained on something else than we have a problem
            if ((!targetVars[0].equals(firstTargetName()) ||
                    k != firstDictionary().length - 1)) {
                throw new IllegalArgumentException("previous classifier trained on different target");
            }
            runs += additionalRuns;
        } else {
            runs = additionalRuns;
            learn(df, weights, targetVarsRange);
            return;
        }

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transValue(x -> x / total);

        for (int i = h.size(); i < runs; i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
        }
    }

    private boolean learnRound(Frame df) {
        int[] rows = getSamplingRows(df);
        Frame dfTrain = MappedFrame.newByRow(df, rows);
        Numeric dfWeights = (Numeric) w.mapRows(rows).solidCopy();

        Classifier hh = base.newInstance();
        hh.learn(dfTrain, dfWeights, targetNames);
        CResult p = hh.predict(df, true, false);
        double err = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (p.firstClasses().index(j) != df.var(firstTargetName()).index(j)) {
                err += w.value(j);
            }
        }
        err /= w.stream().mapToDouble().sum();
        double alpha = Math.log((1. - err) / err) + Math.log(k - 1);
        if (err == 0) {
            if (h.isEmpty()) {
                h.add(hh);
                a.add(alpha);
            }
            return false;
        }
        if (stopOnError && err > (1.0 - 1.0 / k) + delta_error) {
            return false;
        }
        h.add(hh);
        a.add(alpha);

        for (int j = 0; j < w.rowCount(); j++) {
            if (p.firstClasses().index(j) != df.var(firstTargetName()).index(j)) {
                w.setValue(j, w.value(j) * Math.exp(alpha));
            }
        }
        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transValue(x -> x / total);

        return true;
    }

    @Override
    public CResult predict(Frame df, boolean withClasses, boolean withDistributions) {
        CResult p = CResult.newEmpty(this, df, withClasses, true);
        p.addTarget(firstTargetName(), firstDictionary());

        for (int i = 0; i < h.size(); i++) {
            CResult hp = h.get(i).predict(df, true, false);
            for (int j = 0; j < df.rowCount(); j++) {
                int index = hp.firstClasses().index(j);
                p.firstDensity().setValue(j, index, p.firstDensity().value(j, index) + a.get(i));
            }
        }

        // simply predict
        for (int i = 0; i < p.firstDensity().rowCount(); i++) {

            double max = 0;
            int prediction = 0;
            double total = 0;
            for (int j = 1; j < p.firstDensity().varCount(); j++) {
                total += p.firstDensity().value(i, j);
                if (p.firstDensity().value(i, j) > max) {
                    prediction = j;
                    max = p.firstDensity().value(i, j);
                }
            }
            for (int j = 1; j < p.firstDensity().varCount(); j++) {
                p.firstDensity().setValue(i, j, p.firstDensity().value(i, j) / total);
            }
            p.firstClasses().setIndex(i, prediction);
        }
        return p;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");

    }
}
