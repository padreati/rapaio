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

import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.util.collection.Ints;

public class ConvDemo {

    public static void main(String[] args) {

        DArrayManager dm = DArrayManager.base();
        DType<?> dt = DType.FLOAT;

        var x = dm.stride(dt, Shape.of(3, 5, 5), Order.C,
                1, 0, 0, 2, 0,
                1, 1, 0, 0, 2,
                0, 2, 1, 2, 1,
                2, 1, 2, 2, 0,
                2, 0, 1, 1, 1,

                0, 2, 2, 0, 0,
                2, 1, 1, 1, 2,
                0, 1, 2, 1, 1,
                1, 2, 0, 1, 0,
                1, 2, 2, 1, 2,

                0, 2, 0, 2, 1,
                1, 1, 1, 1, 0,
                0, 0, 0, 0, 1,
                1, 2, 2, 2, 1,
                1, 1, 2, 1, 0
        );

        var w = dm.stride(dt, Shape.of(2, 3, 3, 3), Order.C,
                -1, 0, 1,
                -1, -1, 0,
                0, 1, 0,

                0, 1, 0,
                0, 1, 1,
                0, -1, 1,

                1, 1, -1,
                0, 0, 1,
                -1, 0, 0,

                0, -1, -1,
                0, 0, 1,
                -1, 0, 1,

                0, 1, 1,
                -1, 1, 1,
                -1, 1, 1,

                0, -1, 0,
                0, 0, 0,
                1, -1, -1
        );

        var b = dm.stride(dt, Shape.of(2), Order.C, 1, 0);

        x.printString();
        w.printString();
        b.printString();

        var xpad = x.pad(Ints.of(1, 1), Ints.of(1, 1));
        xpad.printString();


        xpad.unfold(1, 3, 2, 1).unfold(2, 3, 2, 1).printString();
    }
}
