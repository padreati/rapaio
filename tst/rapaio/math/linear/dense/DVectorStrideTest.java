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

public class DVectorStrideTest extends StandardDVectorTest {

    @Override
    public VType type() {
        return VType.STRIDE;
    }

    @Override
    public DVector generateCopy(double[] values) {
        return new DVectorStride(0, values.length, 1, values);
    }

    @Override
    public DVector generateSeq(int end) {
        double[] base = new double[3 + end * 2];
        for (int i = 0; i < end; i++) {
            base[3 + i * 2] = i;
        }
        return new DVectorStride(3, end, 2, base);
    }

    @Override
    public DVector generateFill(int size, double fill) {
        return DVector.fill(VType.STRIDE, size, fill);
    }

    @Override
    public String className() {
        return "DVectorStride";
    }
}