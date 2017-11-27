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
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.boost.gbt.BTRegression;
import rapaio.ml.regression.boost.gbt.GBTLossDeviance;
import rapaio.ml.regression.tree.RTree;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/12/14.
 */
public class GBTClassifier extends AbstractClassifier implements Classifier {

    private static final long serialVersionUID = -2979235364091072967L;
    int K;
    double[][] f;
    double[][] p;
    private double shrinkage = 1.0;
    private boolean debug = false;

    // prediction artifact
    private BTRegression classifier = RTree.buildCART().withMaxDepth(4);
    private List<List<BTRegression>> trees;

    public GBTClassifier() {
        withRuns(10);
    }

    @Override
    public GBTClassifier newInstance() {
        return (GBTClassifier) new GBTClassifier()
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
    public boolean coreTrain(Frame df, Var weights) {

        // algorithm described by ESTL pag. 387

        K = firstTargetLevels().length - 1;
        f = new double[K][df.rowCount()];
        p = new double[K][df.rowCount()];
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
            yk.get(df.rvar(firstTargetName()).index(i) - 1).setValue(i, 1);
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
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += f[k][i];
                if(!Double.isFinite(sum)) {
                    WS.println("ERROR");
                }
            }
            for (int k = 0; k < K; k++) {
                p[k][i] = yk.get(k).value(i) - Math.exp(f[k][i]-sum);
                if (Double.isNaN(p[k][i])) {
                    WS.println("ERROR");
                }
            }
        }

        if (debug) {
            WS.println("pks");
            List<Var> pks = new ArrayList<>();
            for (int i = 0; i < p.length; i++) {
                pks.add(NumVar.wrap(p[i]).withName("" + (i + 1)));
            }
            SolidFrame.byVars(pks).printLines(30);
            WS.println("f");
            List<Var> fs = new ArrayList<>();
            for (int i = 0; i < f.length; i++) {
                fs.add(NumVar.wrap(f[i]).withName("" + (i + 1)));
            }
            SolidFrame.byVars(fs).printLines(30);
        }

        // b)

        Frame x = df.removeVars(targetNames());
        Sample sample = sampler().nextSample(x, w);

        for (int k = 0; k < K; k++) {

            NumVar pk = NumVar.wrap(p[k]).withName("##tt##");
            Frame train = sample.df.bindVars(pk.mapRows(sample.mapping));

            BTRegression tree = classifier.newInstance();
            tree.train(train, sample.weights, "##tt##");
            trees.get(k).add(tree);

            tree.boostUpdate(x, pk, pk, new GBTLossDeviance(K));

            RFit rr = tree.fit(df, false);
            for (int i = 0; i < df.rowCount(); i++) {
                f[k][i] += shrinkage * rr.firstFit().value(i);
            }
        }
    }

    @Override
    public CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit cr = CFit.build(this, df, withClasses, withDistributions);
        for (int k = 0; k < K; k++) {
            for (BTRegression tree : trees.get(k)) {
                RFit rr = tree.fit(df, false);
                for (int i = 0; i < df.rowCount(); i++) {
                    double p = cr.firstDensity().value(i, k + 1);
                    p += shrinkage * rr.firstFit().value(i);
                    cr.firstDensity().setValue(i, k + 1, p);
                }
            }
        }

        // make probabilities

        for (int i = 0; i < df.rowCount(); i++) {
            double t = 0.0;
            for (int k = 0; k < K; k++) {
                t += Math.exp(cr.firstDensity().value(i, k + 1));
            }
            if (t != 0) {
                for (int k = 0; k < K; k++) {
                    cr.firstDensity().setValue(i, k + 1, Math.exp(cr.firstDensity().value(i, k + 1)) / t);
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

