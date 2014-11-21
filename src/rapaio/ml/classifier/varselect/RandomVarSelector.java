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

package rapaio.ml.classifier.varselect;

import rapaio.core.sample.Sampling;
import rapaio.data.Frame;
import rapaio.data.VarRange;

import java.util.ArrayList;
import java.util.List;

/**
 * Variable selector which selects only a maximum number of variables at random
 * <p>
 * User: <a href="mailoto:paderati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RandomVarSelector implements VarSelector {

    private final int mVars;
    private String[] candidates;

    public RandomVarSelector(int mVars) {
        this.mVars = mVars;
    }

    public String name() {
        return "Random[" + mVars + "]";
    }

    public synchronized void initialize(Frame df, VarRange except) {
        List<Integer> exceptColumns = except == null ? new ArrayList<>() : except.parseVarIndexes(df);
        candidates = new String[df.varCount() - exceptColumns.size()];
        int pos = 0;
        int expos = 0;
        for (int i = 0; i < df.varCount(); i++) {
            if (expos < exceptColumns.size() && i == exceptColumns.get(expos)) {
                expos++;
                continue;
            }
            candidates[pos++] = df.varNames()[i];
        }
    }

    @Override
    public synchronized String[] nextVarNames() {
        if (mVars < 1) {
            throw new RuntimeException("Uniform random var selector not initialized");
        }
        int[] indexes = Sampling.sampleWR(mVars, candidates.length);
        String[] result = new String[mVars];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = candidates[indexes[i]];
        }
        return result;
    }

    @Override
    public String toString() {
        return "RAND_VAR_SELECTOR(" + mVars + ')';
    }
}
