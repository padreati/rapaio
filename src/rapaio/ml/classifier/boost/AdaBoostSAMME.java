/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.filter.FFilter;
import rapaio.data.sample.Sample;
import rapaio.data.sample.RowSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.Capabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * AdaBoost SAMME classifier is the classical version of AdaBoost which has
 * the correction which works for classification with multiple
 * labels.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMME extends AbstractClassifier {

    private static final long serialVersionUID = -9154973036108114765L;
    private static final double delta_error = 10e-10;

    // parameters

    private Classifier weak = CTree.newCART().withMaxDepth(6).withMinCount(6);
    private boolean stopOnError = false;
    private double shrinkage = 1.0;

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
                .withInputFilters(inputFilters())
                .withClassifier(this.weak.newInstance())
                .withStopOnError(stopOnError)
                .withShrinkage(shrinkage)
                .withSampler(sampler())
                .withRuns(runs())
                .withRunningHook(runningHook())
                .withRunPoolSize(runPoolSize());
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

    public AdaBoostSAMME withSampler(RowSampler sampler) {
        return (AdaBoostSAMME) super.withSampler(sampler);
    }

    public AdaBoostSAMME withStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
        return this;
    }

    public AdaBoostSAMME withShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        k = firstTargetLevels().length - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = weights.solidCopy();

        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        for (int i = 0; i < w.getRowCount(); i++) {
            w.setValue(i, w.getValue(i) / total);
        }

        for (int i = 0; i < runs(); i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
            if (runningHook() != null) {
                runningHook().accept(this, i + 1);
            }
        }
        return true;
    }

    private boolean learnRound(Frame df) {

        Classifier hh = weak.newInstance();

        Sample sample = sampler().nextSample(df, w);
        hh.train(sample.df, sample.weights.solidCopy(), targetNames());

        CFit fit = hh.fit(df, true, false);

        double err = 0;
        for (int j = 0; j < df.getRowCount(); j++) {
            if (fit.firstClasses().getIndex(j) != df.getVar(firstTargetName()).getIndex(j)) {
                err += w.getValue(j);
            }
        }
        err /= w.stream().mapToDouble().sum();
        double alpha = Math.log((1.0 - err) / err) + Math.log(k - 1.0);
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

        for (int j = 0; j < w.getRowCount(); j++) {
            if (fit.firstClasses().getIndex(j) != df.getVar(firstTargetName()).getIndex(j)) {
                w.setValue(j, w.getValue(j) * Math.exp(alpha * shrinkage));
            }
        }
        double total = w.stream().mapToDouble().reduce(0.0, (x, y) -> x + y);
        for (int i = 0; i < w.getRowCount(); i++) {
            w.setValue(i, w.getValue(i) / total);
        }

        return true;
    }

    @Override
    protected CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit fit = CFit.build(this, df, withClasses, true);
        for (int i = 0; i < h.size(); i++) {
            CFit hp = h.get(i).fit(df, true, false);
            for (int j = 0; j < df.getRowCount(); j++) {
                int index = hp.firstClasses().getIndex(j);
                fit.firstDensity().setValue(j, index, fit.firstDensity().getValue(j, index) + a.get(i));
            }
        }

        // simply fit
        for (int i = 0; i < fit.firstDensity().getRowCount(); i++) {

            double max = 0;
            int best = 0;
            double total = 0;
            for (int j = 1; j < fit.firstDensity().getVarCount(); j++) {
                total += fit.firstDensity().getValue(i, j);
                if (fit.firstDensity().getValue(i, j) > max) {
                    best = j;
                    max = fit.firstDensity().getValue(i, j);
                }
            }
            for (int j = 1; j < fit.firstDensity().getVarCount(); j++) {
                fit.firstDensity().setValue(i, j, fit.firstDensity().getValue(i, j) / total);
            }
            fit.firstClasses().setIndex(i, best);
        }
        return fit;
    }

    @Override
    public AdaBoostSAMME withRuns(int runs) {
        return (AdaBoostSAMME) super.withRuns(runs);
    }

    @Override
    public AdaBoostSAMME withRunningHook(BiConsumer<Classifier, Integer> runningHook) {
        return (AdaBoostSAMME) super.withRunningHook(runningHook);
    }

    @Override
    public AdaBoostSAMME withInputFilters(FFilter... filters) {
        return (AdaBoostSAMME) super.withInputFilters(filters);
    }

    @Override
    public AdaBoostSAMME withInputFilters(List<FFilter> filters) {
        return (AdaBoostSAMME) super.withInputFilters(filters);
    }

    @Override
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");
        return sb.toString();
    }
}
