/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.classifier.boost;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.classifier.DefaultHookInfo;
import rapaio.ml.classifier.tree.CTree;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * AdaBoost SAMME classifier is the classical version of AdaBoost which has
 * the correction which works for classification with multiple
 * labels.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class AdaBoost extends ClassifierModel<AdaBoost, ClassifierResult, DefaultHookInfo> implements Printable {

    public static AdaBoost newModel() {
        return new AdaBoost();
    }

    @Serial
    private static final long serialVersionUID = -9154973036108114765L;

    // parameters

    public final ValueParam<ClassifierModel<?, ?, ?>, AdaBoost> model = new ValueParam<>(this,
            CTree.newCART().maxDepth.set(6).minCount.set(6),
            "model",
            "Weak learner model",
            Objects::nonNull);

    public final ValueParam<Double, AdaBoost> eps = new ValueParam<>(this, 10e-10,
            "eps",
            "Threshold value used to decide convergence on fit",
            Double::isFinite);

    public final ValueParam<Boolean, AdaBoost> stopOnError = new ValueParam<>(this, false,
            "stopOnError",
            "Flag to stop fitting on learning error of weak classifier");

    public final ValueParam<Double, AdaBoost> shrinkage = new ValueParam<>(this, 1.0,
            "shrinkage",
            "Shrinkage coefficient for regularization",
            Double::isFinite);

    // model artifacts

    private final List<Double> alphas = new ArrayList<>();

    private final List<ClassifierModel<?, ?, ?>> learners = new ArrayList<>();

    private AdaBoost() {
    }

    @Override
    public AdaBoost newInstance() {
        return new AdaBoost().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "AdaBoost";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, 10_000, Arrays.asList(VarType.DOUBLE, VarType.NOMINAL, VarType.INT, VarType.BINARY), true,
                1, 1, List.of(VarType.NOMINAL), false);
    }

    public List<Double> getAlphas() {
        return alphas;
    }

    public List<ClassifierModel<?, ?, ?>> getLearners() {
        return learners;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        Var w = weights.copy().op().divide(weights.op().nansum());
        double k = firstTargetLevels().size() - 1;

        learners.clear();
        alphas.clear();

        for (int i = 0; i < runs.get(); i++) {
            if (!learnRound(df, w, k)) {
                break;
            }
            if (runningHook.get() != null) {
                runningHook.get().accept(new DefaultHookInfo(this, i));
            }
        }
        return true;
    }

    private boolean learnRound(Frame df, Var w, double k) {

        ClassifierModel<?, ?, ?> hh = model.get().newInstance();

        RowSampler.Sample sample = rowSampler.get().nextSample(df, w);
        hh.fit(sample.df(), sample.weights(), targetNames());

        var predict = hh.predict(df, true, false).firstClasses();

        double err = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            if (predict.getInt(i) != df.getInt(i, firstTargetName())) {
                err += w.getDouble(i);
            }
        }
        err /= w.op().nansum();
        double alpha = shrinkage.get() * (Math.log((1.0 - err) / err) + Math.log(k - 1.0));
        if (stopOnError.get() && err > (1.0 - 1.0 / k) + 1e-10) {
            return false;
        }
        learners.add(hh);
        alphas.add(alpha);
        if (err < eps.get()) {
            return false;
        }

        double factor = Math.exp(alpha);
        for (int j = 0; j < w.size(); j++) {
            if (predict.getInt(j) != df.getInt(j, firstTargetName())) {
                w.setDouble(j, w.getDouble(j) * factor);
            }
        }
        w.op().divide(w.op().nansum());

        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult fit = ClassifierResult.build(this, df, withClasses, true);
        for (int i = 0; i < learners.size(); i++) {
            ClassifierResult hp = learners.get(i).predict(df, true, false);
            for (int j = 0; j < df.rowCount(); j++) {
                int index = hp.firstClasses().getInt(j);
                fit.firstDensity().setDouble(j, index, fit.firstDensity().getDouble(j, index) + alphas.get(i));
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("; fitted=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", fitted trees=").append(learners.size());
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        if (hasLearned()) {
            sb.append("weak learners built: ").append(learners.size()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary();
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary();
    }
}
