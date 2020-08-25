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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
public class MapKernelCache implements KernelCache {

    private static final long serialVersionUID = -654236501370533888L;

    final transient private Map<Frame, Map<Frame, Map<Long, Double>>> cache = new HashMap<>();

    @Override
    public void store(Frame df1, int row1, Frame df2, int row2, double value) {
        if (!cache.containsKey(df1)) {
            cache.put(df1, new HashMap<>());
        }
        if (!cache.get(df1).containsKey(df2)) {
            cache.get(df1).put(df2, new HashMap<>());
        }
        cache.get(df1).get(df2).put((((long) row1) << 32) | (row2 & 0xffffffffL), value);
    }

    @Override
    public Double retrieve(Frame df1, int row1, Frame df2, int row2) {
        if (cache.containsKey(df1) && cache.get(df1).containsKey(df2)) {
            cache.get(df1).get(df2).get((((long) row1) << 32) | (row2 & 0xffffffffL));
        }
        return null;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
