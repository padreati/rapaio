/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.dense;

import rapaio.math.linear.DVector;
import rapaio.math.linear.StandardDVectorTest;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/21.
 */
public class DVectorMapTest extends StandardDVectorTest {

    @Override
    public DVector generateFill(int size, double fill) {
        DVector source = DVector.fill(size, fill);
        return source.map(IntArrays.newSeq(0, source.size()));
    }

    @Override
    public DVector generateSeq(int end) {
        double[] base = new double[3 + end * 2];
        int[] indexes = new int[end];
        for (int i = 0; i < end; i++) {
            indexes[i] = 3 + i * 2;
            base[3 + i * 2] = i;
        }
        return new DVectorMap(DVector.wrap(base), indexes);
    }

    @Override
    public DVector generateCopy(double[] values) {
        DVector source = DVector.wrap(values);
        return new DVectorMap(source, IntArrays.newSeq(0, source.size()));
    }

    @Override
    public String className() {
        return DVectorMap.class.getSimpleName();
    }
}
