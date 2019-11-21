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

package rapaio.data.filter;

import rapaio.core.RandomSource;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.util.collection.IntArrays;

/**
 * Filter which shuffles observations from a given variable. The new variable is
 * a mapped variable over the original one.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VShuffle implements VFilter {

    public static VShuffle filter() {
        return new VShuffle();
    }

    private static final long serialVersionUID = -5571537968976749556L;

    private VShuffle() {
    }

    @Override
    public Var apply(Var var) {
        int[] mapping = new int[var.rowCount()];
        for (int i = 0; i < mapping.length; i++) {
            mapping[i] = i;
        }
        IntArrays.shuffle(mapping, RandomSource.getRandom());
        return var.mapRows(Mapping.wrap(mapping));
    }

    @Override
    public String toContent() {
        return "VShuffle";
    }

    @Override
    public String toString() {
        return toContent();
    }
}
