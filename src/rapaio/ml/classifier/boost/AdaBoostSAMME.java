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

package rapaio.ml.classifier.boost;

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

import java.util.ArrayList;
import java.util.List;

/**
 * AdaBoost SAMME classifier is the classical version of AdaBoost which has
 * the correction which works for classification with multiple
 * labels.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMME extends AbstractClassifier {

    private static final long serialVersionUID = -9154973036108114765L;
    final double delta_error = 10e-10;

    // parameters

    private Classifier weak = CTree.newDecisionStump();
    private boolean stopOnError = false;

    // model artifacts

    private List<Double> a;
    private List<Classifier> h;
    private Var w;
    private double k;

    public AdaBoostSAMME() {
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
        withRuns(10);
    }

    @Override
    public AdaBoostSAMME newInstance() {
        return (AdaBoostSAMME) new AdaBoostSAMME()
                .withClassifier(this.weak.newInstance())
                .withSampler(sampler())
                .withRuns(runs());
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
        sb.append("runs: ").append(runs()).append(", ");
        sb.append("sampler: ").append(sampler().name()).append(", ");
        sb.append("stopOnError: ").append(stopOnError).append(", ");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
                .withInputTypes(VarType.NUMERIC, VarType.NOMINAL, VarType.INDEX, VarType.BINARY)
                .withInputCount(1, 10_000)
                .withAllowMissingInputValues(true)
                .withTargetTypes(VarType.NOMINAL)
                .withTargetCount(1, 1)
                .withAllowMissingTargetValues(false);
    }

    public AdaBoostSAMME withClassifier(Classifier weak) {
        this.weak = weak;
        return this;
    }

    public AdaBoostSAMME withSampler(FrameSampler sampler) {
        return (AdaBoostSAMME) super.withSampler(sampler);
    }

    public AdaBoostSAMME withStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
        return this;
    }

    @Override
    public boolean coreTrain(Frame df, Var weights) {

        k = firstTargetLevels().length - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = weights.solidCopy();

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        w = w.stream().transValue(x -> x / total).toMappedVar();

        for (int i = 0; i < runs(); i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
            if (runningHook() != null) {
                runningHook().accept(this, i);
            }
        }
        return true;
    }

    private boolean learnRound(Frame df) {
        FrameSample sample = sampler().newSample(df, w);
        Frame dfTrain = sample.df;
        Var dfWeights = sample.weights.solidCopy();

        Classifier hh = weak.newInstance();
        hh.train(dfTrain, dfWeights, targetNames());
        CFit p = hh.fit(df, true, false);
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
    public CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit p = CFit.newEmpty(this, df, withClasses, true);
        p.addTarget(firstTargetName(), firstTargetLevels());

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
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");
        return sb.toString();
    }
}
