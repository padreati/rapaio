/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.ml.regression.tree.srt;

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.experiment.ml.regression.tree.SmoothRTree;
import rapaio.ml.common.VarSelector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/19/19.
 */
public class SmoothRTreeNode {

    private final SmoothRTreeNode parent;

    // learning artifacts
    private double leftMargin;
    private double rightMargin;
    private SmoothRFunction function;
    private boolean isLeaf;
    private SmoothRTreeNode leftNode;
    private SmoothRTreeNode rightNode;

    public SmoothRTreeNode(SmoothRTreeNode parent) {
        this.parent = parent;
    }

    public SmoothRFunction getFunction() {
        return function;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public SmoothRTreeNode getLeftNode() {
        return leftNode;
    }

    public SmoothRTreeNode getRightNode() {
        return rightNode;
    }

    public void coreFit(SmoothRTree tree, Frame df, Var weights, Random random) {

        String targetName = tree.firstTargetName();
        int maxDepth = tree.getMaxDepth();

        Var y = df.rvar(targetName);

//        NBRFunction mean = NBRFunction.CONSTANT;
//        VarDouble prediction = mean.fit(df, weights, y, null);
//        this.functions.add(mean);
//        this.factors.add(1.0);
//        Var residual = VarDouble.from(df.rowCount(), r -> y.getDouble(r) - prediction.getDouble(r)).withName(y.name());
//        this.coreNodeFit(df, weights, residual, tree, 1);
        this.coreNodeFit(df, weights, y, tree, 1, random);
    }

    private double composeWeight(double originalWeight, double newWeight) {
        return composeWeightMult(originalWeight, newWeight);
    }

    private double composeWeightMin(double originalWeight, double newWeight) {
        return Math.min(originalWeight, newWeight);
    }

    private double composeWeightMult(double originalWeight, double newWeight) {
        return originalWeight * newWeight;
    }

    private void coreNodeFit(Frame df, Var weights, Var y, SmoothRTree tree, int depth, Random random) {

        VarSelector varSelector = tree.getVarSelector().withVarNames(tree.inputNames());

        // find best fit function
        String[] testVarNames = varSelector.nextVarNames(random);

        double errorScore = tree.getLoss().residualErrorScore(y);

        AtomicReference<Candidate> bc = new AtomicReference<>(null);
        for (String testVarName : testVarNames) {
            Candidate candidate = computeCandidate(df, weights, y, testVarName, tree);
            if (Double.isNaN(candidate.score) || errorScore < candidate.score) {
                continue;
            }
            if (bc.get() == null || bc.get().score > candidate.score) {
                bc.set(candidate);
            }
        }
        // if we can't build a model than we stop
        if (bc.get() == null) {
            isLeaf = true;
            return;
        }

        function = bc.get().function;

        // check if we found something

        // check depth
        if (depth == tree.getMaxDepth() || bc.get().score < tree.getMinScore()) {
            // this is a final leaf because of depth
            isLeaf = true;
            return;
        }

        // we update the model
        isLeaf = false;

        // and perform the split and call learning further on

        Mapping leftRows = Mapping.empty();
        Mapping rightRows = Mapping.empty();

        Var yHat = VarDouble.empty().name(y.name());
        Var leftW = VarDouble.empty();
        Var rightW = VarDouble.empty();
        Var leftY = VarDouble.empty().name(y.name());
        Var rightY = VarDouble.empty().name(y.name());

        for (int i = 0; i < df.rowCount(); i++) {
            double ytrue = y.getDouble(i);
            double lw = composeWeight(weights.getDouble(i), bc.get().function.leftWeight(df, i));
            if (lw >= tree.getMinWeight()) {
                leftRows.add(i);
                leftY.addDouble(function.leftResidual(df, y, i));
                leftW.addDouble(lw);
            }
            double rw = composeWeight(weights.getDouble(i), bc.get().function.rightWeight(df, i));
            if (rw >= tree.getMinWeight()) {
                rightRows.add(i);
                rightY.addDouble(function.rightResidual(df, y, i));
                rightW.addDouble(rw);
            }
        }

        leftNode = new SmoothRTreeNode(this);
        rightNode = new SmoothRTreeNode(this);

        leftNode.coreNodeFit(
                df.mapRows(leftRows).copy(),
                leftW,
                leftY, tree, depth + 1, random);
        rightNode.coreNodeFit(
                df.mapRows(rightRows).copy(),
                rightW,
                rightY, tree, depth + 1, random);
    }

    public double predict(Frame df, int row, SmoothRTree tree, double w) {
        if (function == null) {
            return 0.0;
        }
        double y_hat = function.predict(df, row);
        if (isLeaf) {
            return y_hat;
        }
        double lw = composeWeight(w, function.leftWeight(df, row));
        if (lw >= tree.getMinWeight()) {
            y_hat += leftNode.predict(df, row, tree, lw) * lw;
        }
        double rw = composeWeight(w, function.rightWeight(df, row));
        if (rw >= tree.getMinWeight()) {
            y_hat += rightNode.predict(df, row, tree, rw) * rw;
        }
        return y_hat;
    }

    private Candidate computeCandidate(Frame df, Var weights, Var y, String testVarName, SmoothRTree tree) {
        SmoothRFunction function = tree.getSmoothRFunction().newInstance();
        VarDouble pred = function.fit(df, weights, y, testVarName);
        if (pred == null) {
            return new Candidate(Double.NaN, testVarName, function, null);
        }
        double score = tree.getLoss().errorScore(y, pred);
        return new Candidate(score, testVarName, function, pred);
    }
}

class Candidate {
    public final double score;
    public final String testVarName;
    public final SmoothRFunction function;
    public final VarDouble prediction;


    Candidate(double score, String testVarName, SmoothRFunction function, VarDouble prediction) {
        this.score = score;
        this.testVarName = testVarName;
        this.function = function;
        this.prediction = prediction;
    }
}

