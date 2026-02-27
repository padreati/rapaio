/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.experiment;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Shape;

public class DArraySandbox {

    @Test
    void testArgmax() {
        DArrayManager dm = DArrayManager.base();
        Random random = new Random();
        DArray<Double> a = dm.random(DType.DOUBLE, Shape.of(4,4), random);

        a.printContent();
        System.out.println(a.trace());
        System.out.println(a.diag());

        a.eig().d().printContent();
        System.out.println(a.eig().d().diag().sum());
    }
}
