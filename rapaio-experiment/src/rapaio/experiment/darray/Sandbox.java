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

import java.util.List;
import java.util.Random;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Shape;

public class Sandbox {

    public static void main(String[] args) {
        DArrayManager am = DArrayManager.base();
        DType<?> dt = DType.FLOAT;
        Random random = new Random(42);
        DArray<?> x = am.random(dt, Shape.of(5, 5), random);
        x.printString();

        int axis = 1;

        List<? extends DArray<?>> splits = x.split(axis, true, 0, 1, 2);
        for(var split : splits) {
            split.printString();
        }

        am.cat(dt, axis, splits).printString();
    }
}
