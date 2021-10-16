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

package rapaio.ml.regression.knn;

import static rapaio.math.MathTools.*;
import static rapaio.math.linear.Algebra.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.EuclideanDistance;
import rapaio.ml.regression.DefaultHookInfo;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;

/**
 * Implements K Nearest Neighbour regression and Weighted K Nearest neighbours.
 * <p>
 * Implementation of Weighted KNN follows:
 * <a href="https://epub.ub.uni-muenchen.de/1769/1/paper_399.pdf">Weighted k-Nearest-Neighbor Techniques
 * and Ordinal Classification, Klaus Hechenbichler, Klaus Schliep, 13th October 2004</a>
 * <p>
 * Optimal weights are implemented following
 * <a href="https://arxiv.org/pdf/1101.5783.pdf">OPTIMAL WEIGHTED NEAREST NEIGHBOUR CLASSIFIERS,  Richard J. Samworth, 2012</a>
 */
public class KnnRegression extends RegressionModel<KnnRegression, RegressionResult, DefaultHookInfo> {

    public static KnnRegression newModel() {
        return new KnnRegression();
    }

    public final ValueParam<Integer, KnnRegression> k = new ValueParam<>(this, 1,
            "k", "k number of neighbours", value -> Objects.nonNull(value) && value > 0);

    public final ValueParam<Distance, KnnRegression> distance = new ValueParam<>(this, new EuclideanDistance(),
            "distance", "distance function");

    public final ValueParam<Distance, KnnRegression> wdistance = new ValueParam<>(this, new EuclideanDistance(),
            "weighting", "weighting distance");

    public final ValueParam<Kernel, KnnRegression> kernel = new ValueParam<>(this, Kernel.RECTANGULAR,
            "kernel", "kernel distance used to transform distance into similarity");

    public final ValueParam<Double, KnnRegression> tol = new ValueParam<>(this, 1e-6,
            "tolerance", "tolerance");

    private DVector[] instances;
    private DVector target;

    @Override
    public KnnRegression newInstance() {
        return new KnnRegression().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "KnnRegression";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(1, Integer.MAX_VALUE, List.of(VarType.DOUBLE, VarType.INT, VarType.LONG, VarType.BINARY), false,
                1, 1, List.of(VarType.DOUBLE, VarType.INT, VarType.LONG, VarType.BINARY), false);
    }

    private DVector buildInstance(Frame df, int row) {
        DVector instance = DVector.zeros(inputNames.length);
        for (int j = 0; j < inputNames.length; j++) {
            instance.set(j, df.getDouble(row, inputNames[j]));
        }
        return instance;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        if (df.rowCount() < 2) {
            throw new IllegalArgumentException("Not enough data for regression.");
        }
        this.instances = new DVector[df.rowCount()];
        for (int i = 0; i < df.rowCount(); i++) {
            instances[i] = buildInstance(df, i);
        }
        this.target = DVector.from(df.rvar(targetNames[0]));
        return true;
    }

    private int[] computeTop(DVector[] instances, DVector x, int k) {
        TreeSet<Integer> top = new TreeSet<>((o1, o2) -> {
            double d1 = distance.get().compute(instances[o1], x);
            double d2 = distance.get().compute(instances[o2], x);
            return Double.compare(d1, d2);
        });
        for (int i = 0; i < instances.length; i++) {
            top.add(i);
            if (top.size() > k) {
                top.remove(top.last());
            }
        }
        int[] indexes = new int[k];
        int pos = 0;
        for (int i : top) {
            indexes[pos++] = i;
        }
        return indexes;
    }

    private DVector computeWeights(int[] top, int ref, DVector x) {

        var d = distance.get();
        DVector w = DVector.from(top.length, i -> d.compute(x, instances[top[i]]));

        double wref = d.compute(x, instances[ref]);
        // normalize by k+1 distance
        w.apply(v -> v / wref);
        // cut values to avoid division by zero
        w.apply(v -> min(v, 1 - tol.get()));
        w.apply(v -> max(v, tol.get()));
        // transform into similarity
        w = kernel.get().transform(w, k.get());
        return w;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, double[] quantiles) {
        RegressionResult result = RegressionResult.build(this, df, withResiduals, quantiles);

        VarDouble prediction = result.firstPrediction();
        for (int i = 0; i < prediction.size(); i++) {
            DVector x = buildInstance(df, i);

            int[] topIndexesEx = computeTop(instances, x, k.get() + 1);
            int[] topIndexes = Arrays.copyOf(topIndexesEx, topIndexesEx.length - 1);
            int ref = topIndexesEx[topIndexesEx.length - 1];
            DVector weights = computeWeights(topIndexes, ref, x);
            prediction.setDouble(i, target.map(topIndexes, copy()).mul(weights).sum() / weights.sum());
        }
        result.buildComplete();
        return result;
    }

    // TODO: implement rank W <- (k+1)-t(apply(as.matrix(D),1,rank))
    // TODO: implement optimal W <- rep(optKernel(k, d=d), each=p)
    public enum Kernel {
        INV {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> 1 / v, copy());
            }
        },
        RECTANGULAR {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? 0.5 : 0, copy());
            }
        },
        TRIANGLUAR {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? 1 - v : 0, copy());
            }
        },
        COS {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? PI * cos(v * HALF_PI) / 4 : 0, copy());
            }
        },
        EPANECHNIKOV {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? 0.75 * (1 - v * v) : 0, copy());
            }
        },
        BIWEIGHT {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? 15 * pow(1 - v * v, 2) / 16 : 0, copy());
            }
        },
        TRIWEIGHT {
            @Override
            public DVector transform(DVector d, int k) {
                return d.apply(v -> abs(v) <= 1 ? 35 * pow(1 - v * v, 3) / 32 : 0, copy());
            }
        },
        GAUSSIAN {

            private final Normal normal = Normal.std();

            @Override
            public DVector transform(DVector d, int k) {
                double alpha = 1.0 / (2 * (k + 1));
                double qua = abs(normal.quantile(alpha));
                return d.apply(v -> normal.pdf(v * qua), copy());
            }
        };
        public abstract DVector transform(DVector d, int k);

    }
}
