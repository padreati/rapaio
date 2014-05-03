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

package rapaio.ml.classifier.colselect;

import rapaio.core.ColRange;
import rapaio.core.sample.DiscreteSampling;
import rapaio.data.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RandomColSelector implements ColSelector {

    private final int mCols;
    private String[] candidates;

    public RandomColSelector(int mCols) {
        this.mCols = mCols;
    }

    public synchronized void initialize(Frame df, ColRange except) {
        List<Integer> exceptColumns = except==null ? new ArrayList<>() : except.parseColumnIndexes(df);
        candidates = new String[df.colCount() - exceptColumns.size()];
        int pos = 0;
        int expos = 0;
        for (int i = 0; i < df.colCount(); i++) {
            if (expos < exceptColumns.size() && i == exceptColumns.get(expos)) {
                expos++;
                continue;
            }
            candidates[pos++] = df.colNames()[i];
        }
    }

    @Override
    public synchronized String[] nextColNames() {
        if (mCols < 1) {
            throw new RuntimeException("Uniform random column selector not initialized");
        }
        int[] indexes = new DiscreteSampling().sampleWR(mCols, candidates.length);
        String[] result = new String[mCols];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = candidates[indexes[i]];
        }
        return result;
    }

    @Override
    public String toString() {
        return "RAND_COL_SELECTOR(" + mCols + ')';
    }
}
