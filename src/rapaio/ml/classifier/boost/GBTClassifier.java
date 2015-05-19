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

import rapaio.core.SamplingTool;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.common.Capabilities;
import rapaio.ml.regressor.RegressorFit;
import rapaio.ml.regressor.boost.gbt.BTRegressor;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.ml.regressor.tree.rtree.RTree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/12/14.
 */
@Deprecated
public class GBTClassifier extends AbstractClassifier implements RunningClassifier {

    private int runs = 10;
    private double shrinkage = 1.0;
    private boolean useBootstrap = true;
    private double bootstrapSize = 1.0;
    private BTRegressor classifier = RTree.buildCART().withMaxDepth(4);

    // prediction artifact

    int K;
    double[][] f;
    double[][] p;
    private List<List<BTRegressor>> trees;

    @Override
    public GBTClassifier newInstance() {
        return new GBTClassifier()
                .withRuns(runs);
    }

    @Override
    public String name() {
        return "GBTClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("runs=").append(runs);
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        throw new IllegalArgumentException("not implemented yet");
    }

    public GBTClassifier withTree(BTRegressor rTree) {
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
    public GBTClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    @Override
    public GBTClassifier learn(Frame df, Var weights, String... targetVarNames) {
        prepareLearning(df, weights, targetVarNames);
        if (targetNames().length != 1) {
            throw new IllegalArgumentException("This classifier accepts one and only one target variable.");
        }
        if (runs <= 0) {
            throw new IllegalArgumentException("runs parameter must be greater than 0");
        }

        // algorithm described by ESTL pag. 387

        K = firstDict().length - 1;
        f = new double[df.rowCount()][K];
        p = new double[df.rowCount()][K];
        trees = new ArrayList<>();
        for (int i = 0; i < K; i++) {
            trees.add(new ArrayList<>());
        }
        for (int m = 0; m < runs; m++) {
            buildAdditionalTree(df, weights);
        }
        return this;
    }

    @Override
    public void learnFurther(Frame df, Var weights, String targetVarNames, int runs) {
        if (targetNames() == null) {
            withRuns(runs);
            learn(df, weights, targetVarNames);
            return;
        }

        if (runs <= 0) {
            throw new IllegalArgumentException("runs parameter must be greater than 0");
        }
        if (this.runs >= runs) {
            throw new IllegalArgumentException("runs parameter must be greater than the current runs learned");
        }
        if (f.length != df.rowCount()) {
            throw new IllegalArgumentException("learn further called for different frame");
        }

        // algorithm described by ESTL pag. 387

        for (int m = this.runs; m < runs; m++) {
            buildAdditionalTree(df, weights);
        }
        this.runs = runs;

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

            Numeric r = Numeric.newEmpty().withName("##tt##");
            for (int i = 0; i < df.rowCount(); i++) {
                double y_i = (df.var(firstTargetName()).index(i) == k + 1) ? 1 : 0;
                r.addValue(y_i - p[i][k]);
            }

            Frame x = df.removeVars(new VarRange(targetNames()));
            Frame train = x.bindVars(r);

            BTRegressor tree = classifier.newInstance();

            Frame bootTrain = train;
            Var bootWeights = weights;
            Frame bootX = x;
            Var bootR = r;
            if (useBootstrap) {
                int[] map = SamplingTool.sampleWR((int) (bootstrapSize * df.rowCount()), df.rowCount());
                bootTrain = train.mapRows(map);
                bootWeights = weights.mapRows(map);
                bootX = x.mapRows(map);
                bootR = r.mapRows(map);
            }
            tree.learn(bootTrain, bootWeights, "##tt##");
            tree.boostFit(bootX, bootR, bootR, new ClassifierLossFunction(K));

            RegressorFit rr = tree.predict(train, true);

            for (int i = 0; i < df.rowCount(); i++) {
                f[i][k] += shrinkage * rr.firstFit().value(i);
            }

            trees.get(k).add(tree);
        }
    }

    @Override
    public CFit fit(Frame df, boolean withClasses, boolean withDistributions) {
        CFit cr = CFit.newEmpty(this, df, withClasses, withDistributions);
        for (String targetName : targetNames()) {
            cr.addTarget(targetName, dictionaries().get(targetName));
        }

        for (int k = 0; k < K; k++) {
            List<BTRegressor> predictors = trees.get(k);
            for (BTRegressor tree : predictors) {
                RegressorFit rr = tree.predict(df, false);
                for (int i = 0; i < df.rowCount(); i++) {
                    double p = cr.firstDensity().value(i, k + 1);
                    p += shrinkage * rr.firstFit().value(i);
                    cr.firstDensity().setValue(i, k + 1, p);
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

    @Override
    public CFit fitFurther(CFit fit, Frame df) {
        throw new IllegalArgumentException("not implemented yet");
    }

    @Override
    public void buildPrintSummary(StringBuilder sb) {
        sb.append("Not Implemented");
    }
}

@Deprecated
class ClassifierLossFunction implements GBTLossFunction {

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
