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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.data.transform;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.RowComparators;
import rapaio.data.VarRange;
import rapaio.util.IntComparator;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public final class RefSort extends AbstractTransform {

    public static RefSort by(IntComparator... comparators) {
        return new RefSort(comparators);
    }

    @Serial
    private static final long serialVersionUID = 3579078253849199109L;
    private final IntComparator aggregateComparator;

    private RefSort(IntComparator... comparators) {
        super(VarRange.of("all"));
        this.aggregateComparator = RowComparators.from(comparators);
    }

    @Override
    public RefSort newInstance() {
        return new RefSort(aggregateComparator);
    }

    @Override
    public void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        int[] rowArray = IntArrays.newSeq(0, df.rowCount());
        IntArrays.quickSort(rowArray, 0, df.rowCount(), aggregateComparator);
        return MappedFrame.byRow(df, Mapping.wrap(rowArray));
    }
}
