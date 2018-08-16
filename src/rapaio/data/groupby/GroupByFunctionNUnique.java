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

package rapaio.data.groupby;

import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.data.Frame;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/10/18.
 */
public class GroupByFunctionNUnique implements GroupByFunction {
    @Override
    public String name() {
        return "nunique";
    }

    @Override
    public double compute(Frame src, String varName, IntList rows) {
        Set<String> set = new HashSet<>();
        int varIndex = src.varIndex(varName);
        for (int row : rows) {
            if (src.isMissing(row, varIndex)) {
                continue;
            }
            set.add(src.getLabel(row, varIndex));
        }
        return set.size();
    }
}

