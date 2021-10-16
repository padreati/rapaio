/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.bayes.nb;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/2/20.
 */
public abstract class AbstractEstimator implements Estimator {

    protected final List<String> testVarNames;
    protected boolean hasLearned = false;

    public AbstractEstimator(List<String> testVarNames) {
        this.testVarNames = new ArrayList<>(testVarNames);
    }

    @Serial
    private static final long serialVersionUID = 2641684738382610007L;

    public List<String> getTestNames() {
        return testVarNames;
    }
}
