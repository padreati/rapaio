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

package rapaio.ml.classifier.tree;

import rapaio.data.Frame;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;
import rapaio.ml.classifier.colselect.ColSelector;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class ForestClassifier extends AbstractClassifier implements RunningClassifier {

    int runs = 0;
    boolean oobCompute = false;
    //
    double oobError = Double.NaN;

    @Override
    public Classifier newInstance() {
        return new ForestClassifier()
                .withColSelector(colSelector)
                .withRuns(runs);
    }

    @Override
    public String name() {
        return "ForestClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("(");

        sb.append(")");
        return sb.toString();
    }

    @Override
    public ForestClassifier withColSelector(ColSelector colSelector) {
        super.withColSelector(colSelector);
        return this;
    }

    public ForestClassifier withRuns(int runs) {
        this.runs = runs;
        return this;
    }


    public ForestClassifier withOobError(boolean oobCompute) {
        this.oobCompute = oobCompute;
        return this;
    }

    public boolean getOobCompute() {
        return oobCompute;
    }

    public double getOobError() {
        return oobError;
    }

    @Override
    public void learn(Frame df, String targetColName) {

    }

    @Override
    public void learnFurther(Frame df, String targetName, int runs) {

    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }
}
