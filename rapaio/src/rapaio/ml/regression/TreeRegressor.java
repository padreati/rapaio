package rapaio.ml.regression;

import rapaio.core.ColRange;
import rapaio.core.stat.Mean;
import rapaio.core.stat.StatOnline;
import rapaio.data.*;
import rapaio.filters.RowFilters;
import rapaio.ml.classification.colselect.ColSelector;
import rapaio.ml.classification.colselect.DefaultColSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * This works for numeric attributes only with no missing values.
 * With this restriction it works like CART or C45Classifier regression trees.
 * <p/>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class TreeRegressor extends AbstractRegressor {

    double minWeight = 1;
    TreeNode root;
    Vector fitted;
    ColSelector colSelector;
    String targetColNames;

    public double getMinWeight() {
        return minWeight;
    }

    public TreeRegressor setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    public ColSelector getColSelector() {
        return colSelector;
    }

    public TreeRegressor setColSelector(ColSelector colSelector) {
        this.colSelector = colSelector;
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        this.targetColNames = targetColName;
        root = new TreeNode();
        root.learn(this, df, weights, targetColName);
        if (colSelector == null) {
            colSelector = new DefaultColSelector(df, new ColRange(targetColName));
        }
    }

    @Override
    public void predict(Frame df) {
        fitted = new Numeric(new double[df.rowCount()]);
        for (int i = 0; i < df.rowCount(); i++) {
            fitted.setValue(i, root.predict(df, i));
        }
    }

    @Override
    public Vector getTestFittedValues() {
        return fitted;
    }
}

class TreeNode {
    boolean leaf;
    double pred;
    double eval = Double.MAX_VALUE;
    String splitColName;
    double splitValue;
    double totalWeight;
    TreeNode left;
    TreeNode right;

    public void learn(TreeRegressor parent, Frame df, List<Double> weights, String targetColNames) {
        totalWeight = 0;
        for (int i = 0; i < weights.size(); i++) {
            totalWeight += weights.get(i);
        }

        if (totalWeight < 2 * parent.minWeight) {
            leaf = true;
            pred = new Mean(df.col(targetColNames)).getValue();
            return;
        }

        String[] colNames = parent.colSelector.nextColNames();
        for (String testColName : colNames) {
            if (df.col(testColName).type().isNumeric() && !targetColNames.equals(testColName)) {
                evaluateNumeric(parent, df, weights, targetColNames, testColName);
            }
        }

        // if we have a split
        if (splitColName != null) {
            Mapping leftMapping = new Mapping();
            Mapping rightMapping = new Mapping();
            List<Double> leftWeights = new ArrayList<>();
            List<Double> rightWeights = new ArrayList<>();

            for (int i = 0; i < df.rowCount(); i++) {
                if (df.value(i, splitColName) <= splitValue) {
                    leftMapping.add(df.rowId(i));
                    leftWeights.add(weights.get(i));
                } else {
                    rightMapping.add(df.rowId(i));
                    rightWeights.add(weights.get(i));
                }
            }
            left = new TreeNode();
            right = new TreeNode();
            left.learn(parent, new MappedFrame(df.sourceFrame(), leftMapping), leftWeights, targetColNames);
            right.learn(parent, new MappedFrame(df.sourceFrame(), rightMapping), rightWeights, targetColNames);
            return;
        }

        // else do the default
        leaf = true;
        pred = new Mean(df.col(targetColNames)).getValue();
    }

    private void evaluateNumeric(TreeRegressor parent,
                                 Frame df, List<Double> weights,
                                 String targetColName,
                                 String testColNames) {

        Vector testCol = df.col(testColNames);
        double[] var = new double[df.rowCount()];
        StatOnline so = new StatOnline();
        Vector sort = Vectors.newSeq(df.rowCount());
        sort = RowFilters.sort(sort, RowComparators.numericComparator(testCol, true));
        double w = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            int pos = sort.rowId(i);
            so.update(testCol.value(pos));
            w += weights.get(pos);
            if (i > 0) {
                var[i] = so.getStandardDeviation() * w / totalWeight;
            }
        }
        so.clean();
        w = 0;
        for (int i = df.rowCount() - 1; i >= 0; i--) {
            int pos = sort.rowId(i);
            so.update(testCol.value(pos));
            w += weights.get(pos);
            if (i < df.rowCount() - 1) {
                var[i] += so.getStandardDeviation() * w / totalWeight;
            }
        }
        w = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            int pos = sort.rowId(i);
            w += weights.get(pos);

            if (w >= parent.minWeight && totalWeight - w >= parent.minWeight) {
                if (var[i] < eval && i > 0 && testCol.value(sort.rowId(i - 1)) != testCol.value(sort.rowId(i))) {
                    eval = var[i];
                    splitColName = testColNames;
                    splitValue = testCol.value(pos);
                }
            }
        }
    }

    public double predict(Frame df, int row) {
        if (leaf) {
            return pred;
        }
        if (df.value(row, splitColName) <= splitValue) {
            return left.predict(df, row);
        } else {
            return right.predict(df, row);
        }
    }
}