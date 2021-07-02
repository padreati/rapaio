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
import rapaio.math.linear.VType;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/28/21.
 */
public class DVectorMapTest extends StandardDVectorTest {


    private static final double TOL = 1e-15;

    @Override
    public VType type() {
        return VType.MAP;
    }

    @Override
    public DVector generateFill(int size, double fill) {
        DVector source = DVector.fill(size, fill);
        return source.map(IntArrays.newSeq(0, source.size()));
    }

    @Override
    public DVector generateWrap(double[] values) {
        DVector source = DVector.wrap(values);
        return source.map(IntArrays.newSeq(0, source.size()));
    }

    @Override
    public String className() {
        return "DVectorMap";
    }
}
