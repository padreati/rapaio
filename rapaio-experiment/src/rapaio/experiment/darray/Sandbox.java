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

package rapaio.experiment.darray;

import java.util.Random;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Shape;
import rapaio.util.collection.Ints;

public class Sandbox {

    public static void main(String[] args) {
        DArrayManager am = DArrayManager.base();
        DType<?> dt = DType.DOUBLE;
        Random random = new Random(42);
        DArray<?> x = am.seq(dt, Shape.of(6, 6));
        x.printString();

        int[] pad = Ints.of(1,1);
        int[] dilation = Ints.of(1,1);

        x.pad(pad, dilation).unfold(0, 3, 1).unfold(1, 3, 1).printString();
    }

}
