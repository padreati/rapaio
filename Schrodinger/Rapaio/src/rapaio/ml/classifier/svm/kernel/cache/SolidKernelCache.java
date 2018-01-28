/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.ml.classifier.svm.kernel.cache;

import rapaio.data.Frame;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
public class SolidKernelCache implements KernelCache {

    private static final long serialVersionUID = -1003713236239594088L;

    private final Frame df;
    private Double[][] cache;

    public SolidKernelCache(Frame df) {
        this.df = df;
        cache = new Double[df.rowCount()][df.rowCount()];
    }

    @Override
    public Double retrieve(Frame df1, int row1, Frame df2, int row2) {
        if (df1 != df2)
            return null;
        if (row1 > row2)
            return retrieve(df1, row2, df2, row1);
        if (df1 == this.df)
            return cache[row1][row2];
        return null;
    }

    @Override
    public void store(Frame df1, int row1, Frame df2, int row2, double value) {
        if (df1 != df2) {
            return;
        }
        if (row1 > row2) {
            store(df1, row2, df2, row1, value);
            return;
        }
        if (df1 == this.df) {
            if (cache == null) {
                throw new IllegalArgumentException();
            }
            cache[row1][row2] = value;
        }
    }

    @Override
    public void clear() {
        cache = null;
    }
}
