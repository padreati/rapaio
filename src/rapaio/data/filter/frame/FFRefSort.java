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

package rapaio.data.filter.frame;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.VRange;

import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFRefSort extends AbstractFF {

    private static final long serialVersionUID = 3579078253849199109L;

    private final IntComparator aggregateComparator;

    public FFRefSort(IntComparator... comparators) {
        super(VRange.of("all"));
        this.aggregateComparator = RowComparators.from(comparators);
    }

    @Override
    public FFRefSort newInstance() {
        return new FFRefSort(aggregateComparator);
    }

    @Override
    public void train(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        int[] rowArray = IntStream.range(0, df.rowCount()).toArray();
        IntArrays.quickSort(rowArray, aggregateComparator);
        return MappedFrame.byRow(df, Mapping.wrap(rowArray));
    }
}
