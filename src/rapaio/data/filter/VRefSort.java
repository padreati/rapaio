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

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.Var;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrayTools;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/3/14.
 */
public class VRefSort implements VFilter {

    public static VRefSort from(IntComparator... rowComparators) {
        return new VRefSort(rowComparators);
    }

    private static final long serialVersionUID = -1075060445963356550L;
    private final IntComparator aggregateComparator;

    public VRefSort(IntComparator... rowComparators) {
        if (rowComparators == null || rowComparators.length == 0) {
            aggregateComparator = null;
        } else {
            aggregateComparator = (rowComparators.length == 1) ? rowComparators[0] : RowComparators.from(rowComparators);
        }
    }

    @Override
    public Var apply(Var var) {
        int[] rows = IntArrayTools.newSeq(0, var.rowCount());
        IntArrays.quickSort(rows, 0, var.rowCount(), aggregateComparator == null ? var.refComparator() : aggregateComparator);
        return var.mapRows(Mapping.wrap(rows));
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toString();
    }

    @Override
    public String toString() {
        return "VRefSort";
    }
}
