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
 *
 */

package rapaio.data.filter;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.ml.analysis.LDA;

public class FFLDA implements FFilter {

    private static final long serialVersionUID = 2797285371357486124L;

    private final String[] draftTargetVars;
    private final int k;
    private LDA lda;

    public FFLDA(int k, String... targetVars) {
        this.k = k;
        this.draftTargetVars = targetVars;
    }

    @Override
    public void fit(Frame df) {
        String[] targetVars = new VarRange(draftTargetVars).parseVarNames(df).stream().toArray(String[]::new);

        lda = new LDA();
        lda.learn(df, targetVars);
    }

    @Override
    public Frame apply(Frame df) {
        return lda.fit(df, k);
    }
}
