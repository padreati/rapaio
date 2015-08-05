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

package rapaio.experiment.classifier.boost;

import rapaio.sys.WS;
import rapaio.core.sample.Sample;
import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.experiment.classifier.tree.CTree;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class AdaBoostSAMMEClassifier extends AbstractClassifier implements RunningClassifier {

    private static final long serialVersionUID = -9154973036108114765L;
    final double delta_error = 10e-10;

    // parameters

    private Classifier weak = CTree.newDecisionStump();
    private int runs = 10;
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
                .withClassifier(this.weak.newInstance())
                .withRuns(this.runs)
                .withSampler(sampler());
    }

    @Override
    public String name() {
        return "AdaBoost.SAMME";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("AdaBoost.SAMME {");
        sb.append("weak: ").append(weak.fullName()).append(", ");
        sb.append("runs: ").append(runs).append(", ");
        sb.append("sampler: ").append(sampler().name()).append(", ");
        sb.append("stopOnError: ").append(stopOnError).append(", ");
        sb.append("}");
        return sb.toString();
    }

    public AdaBoostSAMMEClassifier withClassifier(Classifier weak) {
        this.weak = weak;
        return this;
    }

    public AdaBoostSAMMEClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public AdaBoostSAMMEClassifier withSampler(Sampler sampler) {
        return (AdaBoostSAMMEClassifier) super.withSampler(sampler);
    }

    public AdaBoostSAMMEClassifier withStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
        return this;
    }

    @Override
    public AdaBoostSAMMEClassifier learn(Frame df, Var weights, String... targetVars) {

        prepareLearning(df, weights, targetVars);

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }

        k = firstDict().length - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = weights.solidCopy();

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w = w.stream().transValue(x -> x / total).toMappedVar();

        for (int i = 0; i < runs; i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
        }
        return this;
    }

    @Override
    public void learnFurther(int runs, Frame df, Var weights, String... targetVarsRange) {

        List<String> targetVarList = new VarRange(targetVarsRange).parseVarNames(df);
        String[] targetVars = targetVarList.toArray(new String[targetVarList.size()]);

        if (w != null && this.targetNames() != null && firstDict() != null) {
            // if prev trained on something else than we have a problem
            if ((!targetVars[0].equals(firstTargetName()) ||
                    k != firstDict().length - 1)) {
                throw new IllegalArgumentException("previous classifier trained on different target");
            }
            this.runs += runs;
        } else {
            this.runs = runs;
            learn(df, weights, targetVarsRange);
            return;
        }

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w = w.stream().transValue(x -> x / total).toMappedVar();

        for (int i = h.size(); i < runs; i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
        }
    }

    private boolean learnRound(Frame df) {
        Sample sample = sampler().newSample(df, w);
        Frame dfTrain = sample.df;
        Var dfWeights = sample.weights.solidCopy();

        Classifier hh = weak.newInstance();
        hh.learn(dfTrain, dfWeights, targetNames());
        CFit p = hh.fit(df, true, false);
        double err = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (p.firstClasses().index(j) != df.getVar(firstTargetName()).index(j)) {
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
            if (p.firstClasses().index(j) != df.getVar(firstTargetName()).index(j)) {
                w.setValue(j, w.value(j) * Math.exp(alpha));
            }
        }
        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w.stream().transValue(x -> x / total);

        return true;
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit p = CFit.newEmpty(this, df, withClasses, true);
        p.addTarget(firstTargetName(), firstDict());

        for (int i = 0; i < h.size(); i++) {
            CFit hp = h.get(i).fit(df, true, false);
            for (int j = 0; j < df.rowCount(); j++) {
                int index = hp.firstClasses().index(j);
                p.firstDensity().setValue(j, index, p.firstDensity().value(j, index) + a.get(i));
            }
        }

        // simply fit
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
    public CFit fitFurther(CFit fit, Frame df) {
        throw new IllegalArgumentException("not implemented yet");
    }

    @Override
    public void printSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");
        WS.code(sb.toString());
    }
}
