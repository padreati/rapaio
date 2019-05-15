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

package rapaio.experiment.ml.regression.tree.nbrtree;

import rapaio.data.*;
import rapaio.experiment.ml.regression.tree.*;
import rapaio.ml.common.*;
import rapaio.ml.regression.*;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/19.
 */
public class NBRTreeNode implements Serializable {

    private static final long serialVersionUID = -377340948451917779L;

    private final int id;
    private final NBRTreeNode parent;

    // learning artifacts
    private NBRFunction function;
    private boolean isLeaf;
    private NBRTreeNode leftNone;
    private NBRTreeNode rightNode;

    public NBRTreeNode(int id, NBRTreeNode parent) {
        this.id = id;
        this.parent = parent;
    }

    public void coreFit(NestedBoostingRTree tree, Frame df, Var weights) {

        String targetName = tree.firstTargetName();
        VarSelector varSelector = tree.getVarSelector().newInstance();
        varSelector.withVarNames(tree.inputNames());
        int maxDepth = tree.getMaxDepth();

        Var y = df.rvar(targetName);

        this.coreNodeFit(df, weights, y, varSelector, tree);
    }

    private void coreNodeFit(Frame df, Var weights, Var y, VarSelector varSelector, NestedBoostingRTree tree) {
        // find best fit function
        String[] testVarNames = varSelector.nextVarNames();
        for (String testVarName : testVarNames) {
            Candidate candidate = computeCandidate(df, weights, y, testVarName, tree);
        }
    }

    private Candidate computeCandidate(Frame df, Var weights, Var y, String testVarName, NestedBoostingRTree tree) {
        NBRFunction function = tree.getNbrFunction().newInstance();
        VarDouble pred = function.findBestFit(df, weights, y, testVarName, tree.firstTargetName());
        return null;
    }

    public RPrediction corePredict(Frame df, int row, RPrediction prediction) {
        return null;
    }
}

class Candidate {
    public final double score;
    public final String testVarName;
    public final NBRFunction function;


    Candidate(double score, String testVarName, NBRFunction function) {
        this.score = score;
        this.testVarName = testVarName;
        this.function = function;
    }
}