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

import rapaio.data.*;
import rapaio.data.sample.RowSampler;
import rapaio.data.sample.Sample;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.RPrediction;
import rapaio.ml.regression.boost.gbt.BTRegression;
import rapaio.ml.regression.boost.gbt.GBTLossDeviance;
import rapaio.ml.regression.tree.RTree;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/12/14.
 */
@Deprecated
public class GBTClassifier extends AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -2979235364091072967L;
    int K;
    double[][] f;
    double[][] residual;
    private double shrinkage = .2;
    private boolean debug = false;

    // prediction artifact
    private BTRegression classifier = RTree.newCART().withMaxDepth(4).withMinCount(5);
    private List<List<BTRegression>> trees;

    public GBTClassifier() {
    }

    @Override
    public GBTClassifier newInstance() {
        return new GBTClassifier()
                .withSampler(sampler())
                .withShrinkage(shrinkage)
                .withTree(classifier.newInstance())
                .withRuns(runs());
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
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NOMINAL, VarType.ORDINAL, VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withTargetCount(1, 1)
                .withTargetTypes(VarType.NOMINAL)
                .withAllowMissingTargetValues(false);
    }

    public GBTClassifier withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public GBTClassifier withTree(BTRegression rTree) {
        this.classifier = rTree;
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
        f = new double[K][df.rowCount()];
        residual = new double[K][df.rowCount()];
        trees = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            trees.add(new ArrayList<>());
        }

        // build individual regression targets for each class

        List<NumVar> yk = new ArrayList<>();
        for (int k = 0; k < K; k++) {
            yk.add(NumVar.fill(df.rowCount(), 0));
        }
        for (int i = 0; i < df.rowCount(); i++) {
            yk.get(df.index(i, firstTargetName()) - 1).setValue(i, 1);
        }

        for (int m = 0; m < runs(); m++) {
            buildAdditionalTree(df, weights, yk);
            if (runningHook() != null) {
                runningHook().accept(this, m);
            }
        }
        return true;
    }

    private void buildAdditionalTree(Frame df, Var w, List<NumVar> yk) {

        // a) Set p_k(x)

        for (int i = 0; i < df.rowCount(); i++) {
            double max = f[0][i];
            for (int k = 1; k < K; k++) {
                max = Math.max(max, f[k][i]);
            }
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += Math.exp(f[k][i] - max);
                if (!Double.isFinite(sum)) {
                    WS.println("ERROR");
                }
            }
            for (int k = 0; k < K; k++) {
                residual[k][i] = yk.get(k).value(i) - Math.exp(f[k][i] - max) / Math.exp(sum);
            }
        }

        // b)

        Frame x = df.removeVars(targetNames());
        Sample sample = sampler().nextSample(x, w);

        for (int k = 0; k < K; k++) {

            NumVar resk = NumVar.wrap(residual[k]).withName("##tt##");
            Frame train = sample.df.bindVars(resk.mapRows(sample.mapping));

            BTRegression tree = classifier.newInstance();
            tree.fit(train, sample.weights, "##tt##");
            trees.get(k).add(tree);

            tree.boostUpdate(x, resk, resk, new GBTLossDeviance(K));

            RPrediction rr = tree.predict(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                f[k][i] += shrinkage * rr.firstFit().value(i);
            }
        }

        if (debug) {
            System.out.println();
            System.out.println("debug");
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < K; j++) {
                    System.out.print(WS.formatShort(f[j][i]) + " ");
                }
                System.out.println();
            }
        }
    }

    @Override
    public CPrediction corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        CPrediction cr = CPrediction.build(this, df, withClasses, withDistributions);
        for (int k = 0; k < K; k++) {
            for (BTRegression tree : trees.get(k)) {
                RPrediction rr = tree.predict(df, false);
                for (int i = 0; i < df.rowCount(); i++) {
                    double p = cr.firstDensity().value(i, k + 1);
                    p += shrinkage * rr.firstFit().value(i);
                    cr.firstDensity().setValue(i, k + 1, p);
                }
            }
        }

        // make probabilities

        for (int i = 0; i < df.rowCount(); i++) {
            double max = cr.firstDensity().value(i, 1);
            for (int k = 1; k < K; k++) {
                max = Math.max(cr.firstDensity().value(i, k + 1), max);
            }
            double t = 0.0;
            for (int k = 0; k < K; k++) {
                t += Math.exp(cr.firstDensity().value(i, k + 1) - max);
            }
            if (t != 0) {
                for (int k = 0; k < K; k++) {
                    cr.firstDensity().setValue(i, k + 1, Math.exp(cr.firstDensity().value(i, k + 1) - max) / t);
                }
            }
        }

        for (int i = 0; i < df.rowCount(); i++) {
            int maxIndex = 0;
            double maxValue = Double.NEGATIVE_INFINITY;
            for (int k = 0; k < K; k++) {
                if (cr.firstDensity().value(i, k + 1) > maxValue) {
                    maxValue = cr.firstDensity().value(i, k + 1);
                    maxIndex = k + 1;
                }
            }
            cr.firstClasses().setIndex(i, maxIndex);
        }
        return cr;
    }
}

