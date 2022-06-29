/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import java.io.Serial;
import java.util.stream.IntStream;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.VarRange;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class ShuffleRows extends AbstractTransform {

    public static ShuffleRows filter() {
        return new ShuffleRows();
    }

    @Serial
    private static final long serialVersionUID = 3868876807602578584L;

    private ShuffleRows() {
        super(VarRange.all());
    }

    @Override
    public ShuffleRows newInstance() {
        return new ShuffleRows();
    }

    @Override
    public void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        int[] mapping = IntStream.range(0, df.rowCount()).toArray();
        IntArrays.shuffle(mapping, RandomSource.getRandom());
        return df.mapRows(Mapping.wrap(mapping));
    }
}
