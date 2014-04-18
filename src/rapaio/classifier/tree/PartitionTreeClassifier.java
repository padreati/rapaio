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
import rapaio.classifier.Classifier;
import rapaio.data.Frame;

import static rapaio.classifier.tree.CTree.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class PartitionTreeClassifier extends AbstractClassifier {

    // parameters
    private int minCount = 1;
    private int maxDepth = Integer.MAX_VALUE;

    private NominalMethod nominalMethod = NominalMethods.FULL;
    private NumericMethod numericMethod = NumericMethods.BINARY;
    private Function function = Functions.INFO_GAIN;
    private Splitter splitter = Splitters.IGNORE_MISSING;

    // tree root node
    private CPartitionTreeNode root;

    @Override
    public Classifier newInstance() {
        return new PartitionTreeClassifier()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withNominalMethod(nominalMethod)
                .withNumericMethod(numericMethod)
                .withFunction(function)
                .withSplitter(splitter);
    }

    public int getMinCount() {
        return minCount;
    }

    public PartitionTreeClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public PartitionTreeClassifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public NominalMethod getNominalMethod() {
        return nominalMethod;
    }

    public PartitionTreeClassifier withNominalMethod(NominalMethod methodNominal) {
        this.nominalMethod = methodNominal;
        return this;
    }

    public NumericMethod getNumericMethod() {
        return numericMethod;
    }

    public PartitionTreeClassifier withNumericMethod(NumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public Function getFunction() {
        return function;
    }

    public PartitionTreeClassifier withFunction(Function function) {
        this.function = function;
        return this;
    }

    public Splitter getSplitter() {
        return splitter;
    }

    public PartitionTreeClassifier withSplitter(Splitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    public String name() {
        return "PartitionTreeClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("PartitionTreeClassifier (");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth).append(",");
        sb.append("numericMethod=").append(numericMethod.getMethodName()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.getMethodName()).append(",");
        sb.append("function=").append(function.getFunctionName()).append(",");
        sb.append("splitter=").append(splitter.getSplitterName()).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {

        targetCol = targetColName;
        dict = df.col(targetCol).getDictionary();

        root = new CPartitionTreeNode(this, null);
        root.learn(df, maxDepth);
    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}
