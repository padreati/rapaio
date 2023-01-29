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

package rapaio.experiment.foreign;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.MemorySession;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

public class MemorySegmentApp {


    public static void main(String[] args) {

        int size = 10;
        MemorySegment segment = MemorySegment.allocateNative(size * 8, MemorySession.global());

        VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        var v = DoubleVector.fromMemorySegment(species, segment, 0, ByteOrder.nativeOrder());
        v = v.broadcast(2);
        v.intoMemorySegment(segment, 0, ByteOrder.nativeOrder());

        for (int i = 0; i < size; i++) {
            System.out.println(segment.getAtIndex(ValueLayout.JAVA_DOUBLE, i));
        }
    }
}
