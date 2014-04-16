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

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class TreeClassifier extends AbstractClassifier {

    // parameters
    private int minCount = 1;

    private CTreeMethodNominal methodNominal = CTreeMethodNominal.FULL;
    private CTreeMethodNumeric methodNumeric = CTreeMethodNumeric.BINARY;
    private CTreeFunction function = CTreeFunction.INFO_GAIN;

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

    public CTreeMethodNominal getMethodNominal() {
        return methodNominal;
    }

    public TreeClassifier withMethodNominal(CTreeMethodNominal methodNominal) {
        this.methodNominal = methodNominal;
        return this;
    }

    public CTreeMethodNumeric getMethodNumeric() {
        return methodNumeric;
    }

    public TreeClassifier withMethodNumeric(CTreeMethodNumeric methodNumeric) {
        this.methodNumeric = methodNumeric;
        return this;
    }

    public CTreeFunction getFunction() {
        return function;
    }

    public TreeClassifier withFunction(CTreeFunction function) {
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
        sb.append("methodNumeric=").append(methodNumeric.name()).append(",");
        sb.append("methodNominal=").append(methodNominal.name()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {

    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}
