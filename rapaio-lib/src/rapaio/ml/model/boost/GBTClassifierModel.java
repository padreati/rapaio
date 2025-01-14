/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.boost;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.core.param.ValueParam;
import rapaio.darray.DArray;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.ml.loss.KDevianceLossFunction;
import rapaio.ml.loss.L2LossFunction;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.RTree;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/12/14.
 */
public class GBTClassifierModel extends ClassifierModel<GBTClassifierModel, ClassifierResult, RunInfo<GBTClassifierModel>> {

    public static GBTClassifierModel newModel() {
        return new GBTClassifierModel();
    }

    @Serial
    private static final long serialVersionUID = -2979235364091072967L;

    /**
     * Shrinkage regularization value
     */
    public final ValueParam<Double, GBTClassifierModel> shrinkage = new ValueParam<>(this, 1.0, "shrinkage", Double::isFinite);

    /**
     * Enable debug console output during learning
     */
    public final ValueParam<Boolean, GBTClassifierModel> debug = new ValueParam<>(this, false, "debug");

    /**
     * Weak tree model
     */
    public final ValueParam<RTree, GBTClassifierModel> model = new ValueParam<>(this,
            RTree.newCART().maxDepth.set(2).minCount.set(5).loss.set(new L2LossFunction()), "model");

    private int K;
    private DArray<Double> f;
    private DArray<Double> p;
    private DArray<Double> residual;

    private List<List<RTree>> trees;

    private GBTClassifierModel() {
    }

    @Override
    public GBTClassifierModel newInstance() {
        return new GBTClassifierModel().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "GBTClassifier";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 1_000_000, true, VarType.BINARY, VarType.INT, VarType.NOMINAL, VarType.DOUBLE)
                .targets(1, 1, false, VarType.NOMINAL);
    }

    public List<List<RTree>> getTrees() {
        return trees;
    }

    @Override
    public boolean coreFit(Frame df, Var weights) {

        Random random = getRandom();

        // algorithm described by ESTL pag. 387

        K = firstTargetLevels().size();
        f = DArrays.zeros(Shape.of(K, df.rowCount()));
        p = DArrays.zeros(Shape.of(K, df.rowCount()));
        residual = DArrays.zeros(Shape.of(K, df.rowCount()));

        trees = IntStream.range(0, K).mapToObj(i -> new ArrayList<RTree>()).collect(Collectors.toList());

        // build individual regression targets for each class

        final DArray<Double> yk = DArrays.zeros(Shape.of(K, df.rowCount()));
        for (int i = 0; i < df.rowCount(); i++) {
            yk.setDouble(1, df.getInt(i, firstTargetName()), i);
        }

        for (int m = 0; m < runs.get(); m++) {
            buildAdditionalTree(random, df, weights, yk);
            if (runningHook.get() != null) {
                runningHook.get().accept(RunInfo.forClassifier(this, m));
            }
        }
        return true;
    }

    private void buildAdditionalTree(Random random, Frame df, Var w, DArray<Double> yk) {

        // a) Set p_k(x)

        DArray<Double> max = f.t().amax1d(1);

        for (int i = 0; i < df.rowCount(); i++) {
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += Math.exp(f.getDouble(k, i) - max.getDouble(i));
            }
            for (int k = 0; k < K; k++) {
                p.setDouble(Math.exp(f.getDouble(k, i) - max.getDouble(i)) / sum, k, i);
            }
        }
        residual = yk.sub(p);

        // b)

        Frame x = df.removeVars(targetNames);
        RowSampler.Sample sample = rowSampler.get().nextSample(random, x, w);

        for (int k = 0; k < K; k++) {

            Var residual_k = residual.selsq(0, k).dv().mapRows(sample.mapping()).name("##tt##");

            var tree = model.get().newInstance();
            tree.fit(sample.df().bindVars(residual_k), sample.weights(), "##tt##");
            tree.boostUpdate(df, yk.selsq(0, k).dv(), p.selsq(0, k).dv(), new KDevianceLossFunction(K));

            trees.get(k).add(tree);

            var prediction = tree.predict(df, false).firstPrediction();
            for (int i = 0; i < df.rowCount(); i++) {
                f.incDouble(shrinkage.get() * prediction.getDouble(i), k, i);
            }
        }
    }

    @Override
    public ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassifierResult cr = ClassifierResult.build(this, df, withClasses, withDistributions);

        DArray<Double> p_f = DArrays.zeros(Shape.of(K, df.rowCount()));

        for (int k = 0; k < K; k++) {
            for (RegressionModel<?, ?, ?> tree : trees.get(k)) {
                var rr = tree.predict(df, false).firstPrediction();
                for (int i = 0; i < df.rowCount(); i++) {
                    p_f.setDouble(p_f.getDouble(k, i) + shrinkage.get() * rr.getDouble(i), k, i);
                }
            }
        }

        // make probabilities

        DArray<Double> max = p_f.t().amax1d(1);

        for (int i = 0; i < df.rowCount(); i++) {
            double t = 0.0;
            for (int k = 0; k < K; k++) {
                t += Math.exp(p_f.getDouble(k, i) - max.getDouble(i));
            }
            if (t != 0) {
                for (int k = 0; k < K; k++) {
                    cr.firstDensity().setDouble(i, k, Math.exp(p_f.getDouble(k, i) - max.getDouble(i)) / t);
                }
            }
        }

        for (int i = 0; i < df.rowCount(); i++) {
            int maxIndex = 0;
            double maxValue = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < K; k++) {
                if (cr.firstDensity().getDouble(i, k) > maxValue) {
                    maxValue = cr.firstDensity().getDouble(i, k);
                    maxIndex = k;
                }
            }
            cr.firstClasses().setInt(i, maxIndex);
        }
        return cr;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("; fitted=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", fitted trees=").append(trees.get(0).size());
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");
        if (hasLearned()) {
            sb.append("weak learners built: ").append(trees.get(0).size()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toContent(POpt<?>... options) {
        return toSummary();
    }

    @Override
    public String toFullContent(POpt<?>... options) {
        return toSummary();
    }
}

