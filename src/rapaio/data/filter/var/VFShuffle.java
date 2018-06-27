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

package rapaio.data.filter.var;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import rapaio.core.RandomSource;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFShuffle extends AbstractVF {

    private static final long serialVersionUID = -5571537968976749556L;

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        int[] mapping = IntStream.range(0, vars[0].rowCount()).toArray();
        IntArrays.shuffle(mapping, RandomSource.getRandom());
        return vars[0].mapRows(Mapping.wrap(mapping));
    }
}
