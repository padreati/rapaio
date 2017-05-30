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

import rapaio.data.*;
import rapaio.data.sample.RowSampler;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.RFit;
import rapaio.experiment.ml.regression.boost.gbt.BTRegression;
import rapaio.experiment.ml.regression.boost.gbt.GBTLossFunction;
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
        f = new double[df.getRowCount()][K];
        p = new double[df.getRowCount()][K];
        trees = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            trees.add(new ArrayList<>());
        }
        for (int m = 0; m < runs(); m++) {
            buildAdditionalTree(df, weights);
            if (runningHook() != null) {
                runningHook().accept(this, m);
            }
        }
        return true;
    }

    private void buildAdditionalTree(Frame df, Var weights) {

        // a) Set p_k(x)

        for (int i = 0; i < df.getRowCount(); i++) {
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += Math.pow(Math.E, f[i][k]);
            }
            for (int k = 0; k < K; k++) {
                p[i][k] = Math.pow(Math.E, f[i][k]) / sum;
                if (Double.isNaN(p[i][k])) {
                    WS.println("ERROR");
                }
            }
        }

        // b)

        for (int k = 0; k < K; k++) {

            NumericVar r = NumericVar.empty().withName("##tt##");
            for (int i = 0; i < df.getRowCount(); i++) {
                double y_i = (df.getVar(firstTargetName()).getIndex(i) == k + 1) ? 1 : 0;
                r.addValue(y_i - p[i][k]);
            }

            Frame x = df.removeVars(targetNames());
            Frame train = x.bindVars(r);

            BTRegression tree = classifier.newInstance();

            Mapping samplerMapping = sampler().nextSample(x, weights).mapping;
            tree.train(train.mapRows(samplerMapping), weights.mapRows(samplerMapping), "##tt##");

            tree.boostFit(x, r, r, new ClassifierLossFunction(K));

            RFit rr = tree.fit(train, true);

            for (int i = 0; i < df.getRowCount(); i++) {
                f[i][k] += shrinkage * rr.firstFit().getValue(i);
            }
            trees.get(k).add(tree);
        }
    }

    @Override
    public CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit cr = CFit.build(this, df, withClasses, withDistributions);
        for (int k = 0; k < K; k++) {
            for (BTRegression tree : trees.get(k)) {
                RFit rr = tree.fit(df, false);
                for (int i = 0; i < df.getRowCount(); i++) {
                    double p = cr.firstDensity().getValue(i, k + 1);
                    p += shrinkage * rr.firstFit().getValue(i);
                    cr.firstDensity().setValue(i, k + 1, p);
                }
            }
        }

        // make probabilities

        for (int i = 0; i < df.getRowCount(); i++) {
            double t = 0.0;
            for (int k = 0; k < K; k++) {
                t += Math.exp(cr.firstDensity().getValue(i, k + 1));
            }
            if (t != 0) {
                for (int k = 0; k < K; k++) {
                    cr.firstDensity().setValue(i, k + 1, Math.exp(cr.firstDensity().getValue(i, k + 1)) / t);
                }
            }
        }

        for (int i = 0; i < df.getRowCount(); i++) {
            int maxIndex = 0;
            double maxValue = Double.NEGATIVE_INFINITY;
            double total = 0;
            for (int k = 0; k < K; k++) {
                if (cr.firstDensity().getValue(i, k + 1) > maxValue) {
                    maxValue = cr.firstDensity().getValue(i, k + 1);
                    maxIndex = k + 1;
                }
                total += cr.firstDensity().getValue(i, k + 1);
            }
            // this does not work directly since we have also negative scores
            // why is that happening?

//            for (int k = 0; k < K; k++) {
//                double p = cr.firstDensity().getValue(i, k + 1);
//                p /= total;
//                cr.firstDensity().setValue(i, k + 1, p);
//            }
            cr.firstClasses().setIndex(i, maxIndex);
        }
        return cr;
    }
}

class ClassifierLossFunction implements GBTLossFunction {

    private static final long serialVersionUID = -2622054975826334290L;
    private final double K;

    public ClassifierLossFunction(int K) {
        this.K = K;
    }

    @Override
    public String name() {
        return "ClassifierLossFunction";
    }

    @Override
    public double findMinimum(Var y, Var fx) {
        // this must implement

        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.getRowCount(); i++) {
            up += y.getValue(i);
            down += Math.abs(y.getValue(i)) * (1.0 - Math.abs(y.getValue(i)));
        }

        if (down == 0) {
            return 0;
        }

        if (Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }

        return ((K - 1) * up) / (K * down);
    }

    @Override
    public NumericVar gradient(Var y, Var fx) {
        return null;
    }
}
