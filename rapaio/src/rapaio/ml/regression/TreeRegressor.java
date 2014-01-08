package rapaio.ml.regression;

import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.data.Vectors;

import java.util.List;

/**
 * This works for numeric attributes only with no missing values.
 * With this restriction it works like CART or C45 regression trees.
 * <p/>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class TreeRegressor extends AbstractRegressor {

    double minWeight = 1;
    TreeNode root;

    public double getMinWeight() {
        return minWeight;
    }

    public TreeRegressor setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        root = new TreeNode();
        root.learn(this, df, weights, targetColName);
    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public Vector getFittedValues() {
        return null;
    }

    @Override
    public Vector getResiduals() {
        return null;
    }
}

class TreeNode {
    boolean leaf;
    double pred;
    double eval = Double.MAX_VALUE;
    String testColName;
    double splitValue;

    public void learn(TreeRegressor parent, Frame df, List<Double> weights, String targetColName) {
        double total = 0;
        for (int i = 0; i < weights.size(); i++) {
            total += weights.get(i);
        }

        if (total < 2 * parent.minWeight) {
            leaf = true;
            pred = new Mean(Vectors.newNumeric(weights)).getValue();
            return;
        }

        for (String testColName : df.getColNames()) {
            if (df.getCol(testColName).isNumeric() && !targetColName.equals(testColName)) {
                evaluateNumeric(parent, df, weights, targetColName, testColName);
            }
        }
    }

    private void evaluateNumeric(TreeRegressor parent, Frame df, List<Double> weights, String targetColName, String testColName) {

    }
}