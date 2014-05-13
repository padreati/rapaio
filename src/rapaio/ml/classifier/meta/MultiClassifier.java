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

package rapaio.ml.classifier.meta;

import rapaio.data.Frame;
import rapaio.ml.classifier.AbstractClassifier;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.RunningClassifier;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class MultiClassifier extends AbstractClassifier implements RunningClassifier {

    int runs;
    Classifier c;
    // learning artifacts
    String[] targetCols;


    @Override
    public RunningClassifier withRuns(int runs) {
        return null;
    }

    @Override
    public Classifier newInstance() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public String fullName() {
        return null;
    }

    @Override
    public void learn(Frame df, String targetCol) {

    }

    @Override
    public void predict(Frame df) {

    }

    @Override
    public void buildSummary(StringBuilder sb) {

    }

    @Override
    public void learnFurther(Frame df, String targetName, int runs) {

    }
}
