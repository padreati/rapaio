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

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regression.RFit;
import rapaio.ml.regression.boost.gbt.BTRegression;
import rapaio.ml.regression.boost.gbt.GBTLossFunction;
import rapaio.ml.regression.tree.RTree;

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
    double[][] p;
    private double shrinkage = 1.0;

    // prediction artifact
    private boolean useBootstrap = true;
    private double bootstrapSize = 1.0;
    private BTRegression classifier = RTree.buildCART().withMaxDepth(4);
    private List<List<BTRegression>> trees;

    public GBTClassifier() {
        withRuns(10);
    }

    @Override
    public GBTClassifier newInstance() {
        return (GBTClassifier) new GBTClassifier().withRuns(runs());
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
                .withLearnType(Capabilities.LearnType.MULTICLASS_CLASSIFIER)
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

    public GBTClassifier withBootstrap(boolean use) {
        this.useBootstrap = use;
        return this;
    }

    public GBTClassifier withBootstrapSize(double bootstrapSize) {
        this.bootstrapSize = bootstrapSize;
        return this;
    }

    @Override
    public boolean coreTrain(Frame df, Var weights) {

        // algorithm described by ESTL pag. 387

        K = firstTargetLevels().length - 1;
        f = new double[df.rowCount()][K];
        p = new double[df.rowCount()][K];
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

        for (int i = 0; i < df.rowCount(); i++) {
            double sum = 0;
            for (int k = 0; k < K; k++) {
                sum += Math.pow(Math.E, f[i][k]);
            }
            for (int k = 0; k < K; k++) {
                p[i][k] = Math.pow(Math.E, f[i][k]) / sum;
            }
        }

        // b)

        for (int k = 0; k < K; k++) {

            Numeric r = Numeric.empty().withName("##tt##");
            for (int i = 0; i < df.rowCount(); i++) {
                double y_i = (df.var(firstTargetName()).index(i) == k + 1) ? 1 : 0;
                r.addValue(y_i - p[i][k]);
            }

            Frame x = df.removeVars(targetNames());
            Frame train = x.bindVars(r);

            BTRegression tree = classifier.newInstance();

            Frame bootTrain = train;
            Var bootWeights = weights;
            Frame bootX = x;
            Var bootR = r;
            if (useBootstrap) {
                int[] map = SamplingTools.sampleWR(df.rowCount(), (int) (bootstrapSize * df.rowCount()));
                bootTrain = train.mapRows(map);
                bootWeights = weights.mapRows(map);
                bootX = x.mapRows(map);
                bootR = r.mapRows(map);
            }
            tree.train(bootTrain, bootWeights, "##tt##");
            tree.boostFit(bootX, bootR, bootR, new ClassifierLossFunction(K));

            RFit rr = tree.fit(train, true);

            for (int i = 0; i < df.rowCount(); i++) {
                f[i][k] += shrinkage * rr.firstFit().value(i);
            }

            trees.get(k).add(tree);
        }
    }

    @Override
    public CFit coreFit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit cr = CFit.build(this, df, withClasses, withDistributions);
        for (int k = 0; k < K; k++) {
            List<BTRegression> predictors = trees.get(k);
            for (BTRegression tree : predictors) {
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
            double total = 0;
            for (int k = 0; k < K; k++) {
                if (cr.firstDensity().value(i, k + 1) > maxValue) {
                    maxValue = cr.firstDensity().value(i, k + 1);
                    maxIndex = k + 1;
                }
                total += cr.firstDensity().value(i, k + 1);
            }
            // this does not work directly since we have also negative scores
            // why is that happening?

//            for (int k = 0; k < K; k++) {
//                double p = cr.firstDensity().value(i, k + 1);
//                p /= total;
//                cr.firstDensity().setValue(i, k + 1, p);
//            }
            cr.firstClasses().setIndex(i, maxIndex);
        }
        return cr;
    }

    /*
    @Override
    public CFit fitFurther(CFit fit, Frame df) {
        throw new IllegalArgumentException("not implemented yet");
    }
    */
}

@Deprecated
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

        for (int i = 0; i < y.rowCount(); i++) {
            up += y.value(i);
            down += Math.abs(y.value(i)) * (1.0 - Math.abs(y.value(i)));
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
    public Numeric gradient(Var y, Var fx) {
        return null;
    }
}
