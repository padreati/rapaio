/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.classifier.rvm;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/24/21.
 */
public class RVMClassifier extends AbstractClassifierModel<RVMClassifier, ClassifierResult> {

    private static final long serialVersionUID = -7522271488023157133L;

    public enum Method {

    }

    /**
     * Creates a new classifier instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    @Override
    public ClassifierModel newInstance() {
        return new RVMClassifier().copyParameterValues(this);
    }

    /**
     * Returns the classifier name.
     *
     * @return classifier name
     */
    @Override
    public String name() {
        return "RVMClassifier";
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        return false;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        return null;
    }
}
