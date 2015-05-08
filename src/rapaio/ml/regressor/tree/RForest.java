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

package rapaio.ml.regressor.tree;

import rapaio.printer.Printable;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.regressor.AbstractRegressor;
import rapaio.ml.regressor.Regressor;
import rapaio.ml.regressor.RegressorFit;
import rapaio.ml.regressor.RunningRegressor;
import rapaio.ml.regressor.tree.rtree.RTree;
import rapaio.printer.Printer;

import java.util.ArrayList;
import java.util.List;

import static rapaio.WS.formatFlex;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/15/15.
 */
@Deprecated
public class RForest extends AbstractRegressor implements RunningRegressor, Printable {

    int runs = 0;
    boolean oobCompute = false;
    Regressor r = RTree.buildC45();
    Sampling sampling = Sampling.NONE;
    double samplePercent = 1.0;
    //
    double totalOobInstances = 0;
    double totalOobError = 0;
    double oobError = Double.NaN;
    List<Classifier> predictors = new ArrayList<>();


    @Override
    public Regressor newInstance() {
        return null;
    }

    @Override
    public String name() {
        return "RForest";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append("r=").append(r.fullName()).append(", ");
        sb.append("sampling=").append(sampling).append(", ");
        if (!sampling.equals(Sampling.NONE)) {
            sb.append("samplePercent=").append(formatFlex(samplePercent)).append(", ");
        }
        sb.append("oobComp=").append(oobCompute).append(", ");
        sb.append("runs=").append(runs).append(", ");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {

    }

    @Override
    public RegressorFit predict(Frame df, boolean withResiduals) {
        return null;
    }

    @Override
    public RunningRegressor withRuns(int runs) {
        return null;
    }

    @Override
    public void learnFurther(Frame df, Var weights, int runs, String... targetVars) {

    }

    public static enum Sampling {
        NONE, BOOTSTRAP, RANDOM
    }
}
