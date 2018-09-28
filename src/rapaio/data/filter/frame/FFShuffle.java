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

package rapaio.data.filter.frame;

import it.unimi.dsi.fastutil.ints.IntArrays;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.VRange;
import rapaio.data.filter.FFilter;

import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFShuffle extends AbstractFF {

    private static final long serialVersionUID = 3868876807602578584L;

    public FFShuffle() {
        super(VRange.all());
    }

    @Override
    public FFilter newInstance() {
        return new FFShuffle();
    }

    @Override
    public void fit(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        int[] mapping = IntStream.range(0, df.rowCount()).toArray();
        IntArrays.shuffle(mapping, RandomSource.getRandom());
        return df.mapRows(Mapping.wrap(mapping));
    }
}
