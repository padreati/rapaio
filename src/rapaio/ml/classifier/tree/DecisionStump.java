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
 */

package rapaio.ml.classifier.tree;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.ClassifierFit;
import rapaio.ml.classifier.tree.impl.*;
import rapaio.ml.common.VarSelector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/16/15.
 */
public class DecisionStump extends AbstractClassifier {

    private static final long serialVersionUID = 6153722455097163268L;

    private final CTree tree = new CTree()
            .withMaxDepth(1)
            .withVarSelector(VarSelector.ALL)
            .withTestCounter(new CTreeTestCounter.OneNominalOneNumeric())
            .withSplitter(new CTreeSplitter.RemainsToAllWeighted())
            .withNominalMethod(new CTreeNominalMethod.Binary())
            .withNumericMethod(new CTreeNumericMethod.Binary())
            .withPredictor(new CTreePredictor.Standard());

    @Override
    public Classifier newInstance() {
        return new DecisionStump();
    }

    @Override
    public String name() {
        return "DecisionStump";
    }

    @Override
    public String fullName() {
        return name();
    }

    @Override
    public void learn(Frame df, Var weights, String... targetVarNames) {
        tree.learn(df, weights, targetVarNames);
    }

    @Override
    public ClassifierFit predict(Frame df, boolean withClasses, boolean withDistributions) {
        return tree.predict(df, withClasses, withDistributions);
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        tree.buildSummary(sb);
    }
}
