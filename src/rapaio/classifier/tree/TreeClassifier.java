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
public class TreeClassifier extends AbstractClassifier {

    // parameters
    private int minCount = 1;

    private NominalMethod methodNominal = NominalMethods.FULL;
    private NumericMethod methodNumeric = NumericMethods.BINARY;
    private Function function = Functions.INFO_GAIN;

    // tree root node
    private CTreeNode root;

    @Override
    public Classifier newInstance() {
        return new TreeClassifier()
                .withMinCount(minCount)
                .withMethodNominal(methodNominal)
                .withMethodNumeric(methodNumeric)
                .withFunction(function);
    }

    public int getMinCount() {
        return minCount;
    }

    public TreeClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public NominalMethod getMethodNominal() {
        return methodNominal;
    }

    public TreeClassifier withMethodNominal(NominalMethod methodNominal) {
        this.methodNominal = methodNominal;
        return this;
    }

    public NumericMethod getMethodNumeric() {
        return methodNumeric;
    }

    public TreeClassifier withMethodNumeric(NumericMethod numericMethod) {
        this.methodNumeric = numericMethod;
        return this;
    }

    public Function getFunction() {
        return function;
    }

    public TreeClassifier withFunction(Function function) {
        this.function = function;
        return this;
    }

    @Override
    public String name() {
        return "TreeClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("TreeClassifier (");
        sb.append("numericMethod=").append(methodNumeric.getMethodName()).append(",");
        sb.append("nominalMethod=").append(methodNominal.getMethodName()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("function=").append(function.getFunctionName()).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {

        targetCol = targetColName;
        dict = df.col(targetCol).getDictionary();

        root = new CTreeNode();
        root.learn(this, df, null);
    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}
