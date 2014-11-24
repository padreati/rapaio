/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.regressor.tree.rtree;

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.RPrediction;
import rapaio.ml.regressor.Regressor;

/**
 * Implements a regression tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTree extends AbstractRegressor {

    private RTreePredictor predictor = RTreePredictor.STANDARD;
    RTreeNode root;

    @Override
    public Regressor newInstance() {
        return null;
    }

    @Override
    public String name() {
        return "RTree";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append("(");
        sb.append("varSelector=").append(varSelector.name()).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, Numeric weights, String... targetVarNames) {

    }

    @Override
    public RPrediction predict(Frame df, boolean withResiduals) {
        RPrediction pred = RPrediction.newEmpty(df.rowCount(), withResiduals, targetNames);

        df.stream().forEach(spot -> {
            double result = predictor.predict(this, spot, root);
            pred.fit(firstTargetVarName()).setValue(spot.row(), result);
        });
        pred.buildResiduals(df);
        return pred;
    }
}
