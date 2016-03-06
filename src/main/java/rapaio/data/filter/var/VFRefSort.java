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

package rapaio.data.filter.var;

import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public class VFRefSort extends AbstractVF {

    private static final long serialVersionUID = -1075060445963356550L;
    private final Comparator<Integer> aggregateComparator;

    @SafeVarargs
    public VFRefSort(Comparator<Integer>... rowComparators) {
        if (rowComparators == null || rowComparators.length == 0) {
            throw new IllegalArgumentException("Filter requires at least a row comparator");
        }
        aggregateComparator = (rowComparators.length == 1)
                ? rowComparators[0] : RowComparators.aggregate(rowComparators);
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        List<Integer> rows = new ArrayList<>(vars[0].rowCount());
        for (int i = 0; i < vars[0].rowCount(); i++) {
            rows.add(i);
        }
        Collections.sort(rows, aggregateComparator);
        return vars[0].mapRows(Mapping.wrap(rows));
    }
}
