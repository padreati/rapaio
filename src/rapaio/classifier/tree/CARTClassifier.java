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

package rapaio.classifier.tree;

import rapaio.classifier.AbstractClassifier;
import rapaio.classifier.tools.CTreeTest;
import rapaio.classifier.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Numeric;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CARTClassifier extends AbstractClassifier {

    // parameters
    CTreeTest.Method method = CTreeTest.Method.GINI;
    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;
    // tree
    private CARTNode root;

    @Override
    public CARTClassifier newInstance() {
        return new CARTClassifier();
    }

    public CARTClassifier withMethod(CTreeTest.Method method) {
        this.method = method;
        return this;
    }

    public CARTClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public CARTClassifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    @Override
    public String name() {
        return "CART";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder("CART(");
        sb.append("method=").append(method.name()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {
        this.dict = df.col(targetColName).getDictionary();
        this.targetCol = targetColName;

        this.root = new CARTNode(this, null);
        root.learn(df);
    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}

class CARTNode {

    private final CARTClassifier c;
    private final CARTNode parent;
    private final CTreeTest test;
    //
    boolean leaf;
    DensityVector density;
    DensityVector count;
    int bestIndex;

    CARTNode(CARTClassifier c, CARTNode parent) {
        this.c = c;
        this.parent = parent;
        this.test = new CTreeTest(c.method, c.minCount);
    }

    public void learn(Frame df) {
        leaf = true;
        density = new DensityVector(df.col(c.getTargetCol()), df.getWeights());
        count = new DensityVector(df.col(c.getTargetCol()), new Numeric(df.rowCount(), df.rowCount(), 1));
        bestIndex = density.findBestIndex();

        if (df.rowCount() <= c.minCount || count.countValues(x -> x > 0) == 1) {
            return;
        }

        for (String testCol : df.colNames()) {
            if (testCol.equals(c.getTargetCol())) continue;
            if (df.col(testCol).type().isNumeric()) {
                test.binaryNumericTest(df, testCol, c.getTargetCol());
            } else {
                for (String testLabel : df.col(testCol).getDictionary())
                    test.binaryNominalTest(df, testCol, c.getTargetCol(), testLabel);
            }
        }

        if (test.testName() == null) {
            return;
        }

        if (df.col(test.testName()).type().isNumeric()) {

        } else {

        }
    }
}