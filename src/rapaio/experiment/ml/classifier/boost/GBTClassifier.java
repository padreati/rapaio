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
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.sample.RowSampler;
import rapaio.data.sample.Sample;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRM;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.ClassResult;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.RegResult;
import rapaio.experiment.ml.regression.loss.KDevianceRegressionLoss;
import rapaio.ml.regression.tree.RTree;
import rapaio.printer.*;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/12/14.
 */
public class GBTClassifier extends AbstractClassifier implements Classifier, DefaultPrintable {

    private static final long serialVersionUID = -2979235364091072967L;

    // builders

    public static GBTClassifier newGBT() {
        return new GBTClassifier();
    }

    // parameters

    private double shrinkage = .2;
    private boolean debug = false;
    private RTree rTree = RTree.newCART().withMaxDepth(4).withMinCount(5).withRegressionLoss(new KDevianceRegressionLoss(-1));

    // learning artifacts

    private int K;
    private RM f;
    private RM residual;
    private List<List<RTree>> trees;

    private GBTClassifier() {
    }

    @Override
    public GBTClassifier newInstance() {
        return newInstanceDecoration(new GBTClassifier())
                .withShrinkage(shrinkage)
                .withDebug(debug)
                .withRTree(rTree.newInstance().withRegressionLoss(new KDevianceRegressionLoss(-1)));
    }

    @Override
    public String name() {
        return "GBTClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("runs=").append(runs());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(1, 1_000_000)
                .withInputTypes(VType.BINARY, VType.INT, VType.NOMINAL, VType.DOUBLE)
                .withAllowMissingInputValues(true)
                .withTargetCount(1, 1)
                .withTargetTypes(VType.NOMINAL)
                .withAllowMissingTargetValues(false);
    }

    public GBTClassifier withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public GBTClassifier withRTree(RTree rTree) {
        this.rTree = rTree;
        this.rTree.withRegressionLoss(new KDevianceRegressionLoss(-1));
        return this;
    }

    public GBTClassifier withShrinkage(double shrinkage) {
        this.shrinkage = shrinkage;
        return this;
    }

    @Override
    public GBTClassifier withSampler(RowSampler sampler) {
        return (GBTClassifier) super.withSampler(sampler);
    }

    @Override
    public GBTClassifier withRuns(int runs) {
        return (GBTClassifier) super.withRuns(runs);
    }

    @Override
    public boolean coreFit(Frame df, Var weights) {

        // algorithm described by ESTL pag. 387

        K = firstTargetLevels().size() - 1;
        f = SolidRM.empty(K, df.rowCount());
        residual = SolidRM.empty(K, df.rowCount());
        trees = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            trees.add(new ArrayList<>());
        }

        // build individual regression targets for each class

        SolidRM yk = SolidRM.empty(K, df.rowCount());
        for (int i = 0; i < df.rowCount(); i++) {
            yk.set(df.getInt(i, firstTargetName()) - 1, i, 1);
        }

        for (int m = 0; m < runs(); m++) {
            buildAdditionalTree(df, weights, yk);
            if (runningHook() != null) {
                runningHook().accept(this, m);
            }
        }
        return true;
    }

    private void buildAdditionalTree(Frame df, Var w, RM yk) {

        // a) Set p_k(x)

        RV max = f.t().rowValueMax();
        for (int i = 0; i < df.rowCount(); i++) {
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += Math.exp(f.get(k,i) - max.get(i));
                if (!Double.isFinite(sum)) {
                    WS.println("ERROR");
                }
            }
            for (int k = 0; k < K; k++) {
                residual.set(k,i, yk.get(k,i) -
                        Math.exp(f.get(k,i) - max.get(i)) / Math.exp(sum));
            }
        }

        // b)

        Frame x = df.removeVars(VRange.of(targetNames()));
        Sample sample = sampler().nextSample(x, w);

        for (int k = 0; k < K; k++) {

            Var resk = residual.mapRow(k).asNumericVar().withName("##tt##");
            Frame train = sample.df.bindVars(resk.mapRows(sample.mapping));

            RTree tree = rTree.newInstance().withRegressionLoss(new KDevianceRegressionLoss(K));
            tree.fit(train, sample.weights, "##tt##");
            trees.get(k).add(tree);

            RegResult rr = tree.predict(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                f.increment(k,i, shrinkage * rr.firstPrediction().getDouble(i));
            }
        }
    }

    @Override
    public ClassResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        ClassResult cr = ClassResult.build(this, df, withClasses, withDistributions);

        RM p_f = SolidRM.empty(K, df.rowCount());

        for (int k = 0; k < K; k++) {
            for (RTree tree : trees.get(k)) {
                RegResult rr = tree.predict(df, false);
                for (int i = 0; i < df.rowCount(); i++) {
                    p_f.increment(k, i, shrinkage * rr.firstPrediction().getDouble(i));
                }
            }
        }

        // make probabilities

        RV max = p_f.t().rowValueMax();

        for (int i = 0; i < df.rowCount(); i++) {
            double t = 0.0;
            for (int k = 0; k < K; k++) {
                t += Math.exp(p_f.get(k, i) - max.get(i));
            }
            if (t != 0) {
                for (int k = 0; k < K; k++) {
                    cr.firstDensity().setDouble(i, k + 1, Math.exp(p_f.get(k, i) - max.get(i)) / t);
                }
            }
        }

        for (int i = 0; i < df.rowCount(); i++) {
            int maxIndex = 0;
            double maxValue = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < K; k++) {
                if (cr.firstDensity().getDouble(i, k + 1) > maxValue) {
                    maxValue = cr.firstDensity().getDouble(i, k + 1);
                    maxIndex = k + 1;
                }
            }
            cr.firstClasses().setInt(i, maxIndex);
        }
        return cr;
    }
}

