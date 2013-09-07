/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.data.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class AggregateRowComparator implements Comparator<Integer>, Serializable {

    private final Comparator<Integer>[] comparators;

    public AggregateRowComparator(Comparator<Integer>[] comparators) {
        this.comparators = comparators;
    }

    @Override
    public int compare(Integer row1, Integer row2) {
        for (Comparator<Integer> comparator : comparators) {
            int comp = comparator.compare(row1, row2);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }
}
