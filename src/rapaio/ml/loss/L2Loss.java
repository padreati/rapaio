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

package rapaio.ml.loss;

import static rapaio.util.collection.DoubleArrays.sum;

import java.io.Serial;

import rapaio.core.stat.Mean;
import rapaio.core.stat.WeightedMean;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.model.tree.RTreeLoss;
import rapaio.ml.model.tree.rtree.SearchPayload;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/6/18.
 */
public class L2Loss implements Loss, RTreeLoss {

    @Serial
    private static final long serialVersionUID = 5089493401870663231L;

    @Override
    public String name() {
        return "L2";
    }

    @Override
    public double scalarMinimizer(Var y) {
        return Mean.of(y).value();
    }

    @Override
    public double scalarMinimizer(Var y, Var weight) {
        return WeightedMean.of(y, weight).value();
    }

    @Override
    public double additiveScalarMinimizer(Var y, Var fx) {
        return Mean.of(gradient(y, fx)).value();
    }

    @Override
    public VarDouble gradient(Var y, Var y_hat) {
        int len = Math.min(y.size(), y_hat.size());
        return VarDouble.from(y.size(), row -> y.getDouble(row) - y_hat.getDouble(row));
    }

    @Override
    public VarDouble error(Var y, Var y_hat) {
        int len = Math.min(y.size(), y_hat.size());
        return VarDouble.from(len, row -> Math.pow(y.getDouble(row) - y_hat.getDouble(row), 2) / 2);
    }

    @Override
    public double errorScore(Var y, Var y_hat) {
        double len = Math.min(y.size(), y_hat.size());
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            sum += Math.pow(y.getDouble(i) - y_hat.getDouble(i), 2);
        }
        return Math.sqrt(sum / (2 * len));
    }

    @Override
    public double residualErrorScore(Var residual) {
        double len = residual.size();
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            sum += Math.pow(residual.getDouble(i), 2);
        }
        return Math.sqrt(sum / (2 * len));
    }

    @Override
    public boolean equalOnParams(Loss object) {
        return true;
    }

    @Override
    public double computeSplitLossScore(SearchPayload payload) {
        double down = sum(payload.splitWeight, 0, payload.splitWeight.length);
        double up = 0.0;
        for (int i = 0; i < payload.splits; i++) {
            up += payload.splitWeight[i] * payload.splitVar[i];
        }
        return (down == 0) ? 0.0 : payload.totalVar - up / down;
    }
}
