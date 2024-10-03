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

package rapaio.experiment.math;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

public class MemorySegmentSandbox {

    public static void main(String[] args) {


        VectorSpecies<Double> vsd = DoubleVector.SPECIES_PREFERRED;
        int[] indexes = new int[vsd.length()];
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = i * 2;
        }

        double[] array = new double[1000];
        // read with index map elements 2 by 2
        DoubleVector.fromArray(vsd, array, 0, indexes, 0);

        MemorySegment ms = MemorySegment.ofArray(array);
        // no possible to read with index map
        vsd.fromMemorySegment(ms, 0, ByteOrder.nativeOrder());
    }
}
