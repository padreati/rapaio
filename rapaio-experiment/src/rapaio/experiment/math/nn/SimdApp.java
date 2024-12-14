/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.nn;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorMask;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;
import rapaio.darray.Simd;

public class SimdApp {

    public static void main(String[] args) {


        int[] buffer = new int[542213];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = i;
        }

        VectorSpecies<Integer> vs = Simd.vsi;
        int i = 0;

        int simdBound = vs.loopBound(buffer.length);
        int simdLen = vs.length();

        IntVector vsum = Simd.zeroInt();
        for (; i < simdBound; i += simdLen) {
            IntVector v = IntVector.fromArray(vs, buffer, i);
            vsum = vsum.add(v);
        }
        int sum = vsum.reduceLanes(VectorOperators.ADD);
        if (i < buffer.length) {
            VectorMask<Integer> m = vs.indexInRange(i, buffer.length);
            IntVector v = IntVector.fromArray(vs, buffer, i, m);
            sum += v.reduceLanes(VectorOperators.ADD, m);
        }

        System.out.println(sum);

        int s = 0;
        for (int j = 0; j < buffer.length; j++) {
            s += j;
        }
        System.out.println(s);
    }
}
