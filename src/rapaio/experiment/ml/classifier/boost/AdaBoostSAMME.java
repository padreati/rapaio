/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.experiment.ml.classifier.boost;

import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.sample.Sample;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AdaBoost SAMME classifier is the classical version of AdaBoost which has
 * the correction which works for classification with multiple
 * labels.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoostSAMME extends AbstractClassifierModel<AdaBoostSAMME, ClassifierResult> implements Printable {

    private static final long serialVersionUID = -9154973036108114765L;
    private static final double delta_error = 10e-10;

    // parameters

    private ClassifierModel weak = CTree.newCART().maxDepth.set(6).minCount.set(6);
    private boolean stopOnError = false;
    private double shrinkage = 1.0;

    // model artifacts

    private List<Double> a;
    private List<ClassifierModel> h;
    private Var w;
    private double k;

    public AdaBoostSAMME() {
        this.a = new ArrayList<>();
        this.h = new ArrayList<>();
    }

    @Override
    public AdaBoostSAMME newInstance() {
        return new AdaBoostSAMME().copyParameterValues(this)
                .withClassifier(this.weak.newInstance())
                .withStopOnError(stopOnError)
                .withShrinkage(shrinkage);
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
        sb.append("runs: ").append(runs.get()).append(", ");
        sb.append("sampler: ").append(rowSampler.get().name()).append(", ");
        sb.append("stopOnError: ").append(stopOnError).append(", ");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .inputTypes(Arrays.asList(VType.DOUBLE, VType.NOMINAL, VType.INT, VType.BINARY))
                .minInputCount(1).maxInputCount(10_000)
                .allowMissingInputValues(true)
                .targetType(VType.NOMINAL)
                .minTargetCount(1).maxTargetCount(1)
                .allowMissingTargetValues(false)
                .build();
    }

    public AdaBoostSAMME withClassifier(ClassifierModel weak) {
        this.weak = weak;
        return this;
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
    protected boolean coreFit(Frame df, Var weights) {

        k = firstTargetLevels().size() - 1;

        h = new ArrayList<>();
        a = new ArrayList<>();
        w = weights.copy();

        double total = w.stream().mapToDouble().reduce(0.0, Double::sum);
        for (int i = 0; i < w.rowCount(); i++) {
            w.setDouble(i, w.getDouble(i) / total);
        }

        for (int i = 0; i < runs.get(); i++) {
            boolean success = learnRound(df);
            if (!success && stopOnError) {
                break;
            }
            if (runningHook.get() != null) {
                runningHook.get().accept(this, i + 1);
            }
        }
        return true;
    }

    private boolean learnRound(Frame df) {

        ClassifierModel hh = weak.newInstance();

        Sample sample = rowSampler.get().nextSample(df, w);
        hh.fit(sample.df, sample.weights.copy(), targetNames());

        ClassifierResult fit = hh.predict(df, true, false);

        double err = 0;
        for (int j = 0; j < df.rowCount(); j++) {
            if (fit.firstClasses().getInt(j) != df.getInt(j, firstTargetName())) {
                err += w.getDouble(j);
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

        for (int j = 0; j < w.rowCount(); j++) {
            if (fit.firstClasses().getInt(j) != df.getInt(j, firstTargetName())) {
                w.setDouble(j, w.getDouble(j) * Math.exp(alpha * shrinkage));
            }
        }
        double total = w.stream().mapToDouble().reduce(0.0, Double::sum);
        for (int i = 0; i < w.rowCount(); i++) {
            w.setDouble(i, w.getDouble(i) / total);
        }

        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult fit = ClassifierResult.build(this, df, withClasses, true);
        for (int i = 0; i < h.size(); i++) {
            ClassifierResult hp = h.get(i).predict(df, true, false);
            for (int j = 0; j < df.rowCount(); j++) {
                int index = hp.firstClasses().getInt(j);
                fit.firstDensity().setDouble(j, index, fit.firstDensity().getDouble(j, index) + a.get(i));
            }
        }

        // simply predict
        for (int i = 0; i < fit.firstDensity().rowCount(); i++) {

            double max = 0;
            int best = 0;
            double total = 0;
            for (int j = 1; j < fit.firstDensity().varCount(); j++) {
                total += fit.firstDensity().getDouble(i, j);
                if (fit.firstDensity().getDouble(i, j) > max) {
                    best = j;
                    max = fit.firstDensity().getDouble(i, j);
                }
            }
            for (int j = 1; j < fit.firstDensity().varCount(); j++) {
                fit.firstDensity().setDouble(i, j, fit.firstDensity().getDouble(i, j) / total);
            }
            fit.firstClasses().setInt(i, best);
        }
        return fit;
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append("weak learners built: ").append(h.size()).append("\n");
        return sb.toString();
    }
}
