/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.operator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

public class BroadcastTest {

    @Test
    void testElementWiseValidation() {
        Tensor<Double>[] tensors = new Tensor[] {
                Tensors.seq(Shape.of(2, 1)),
                Tensors.seq(Shape.of(3, 2, 3)),
                Tensors.seq(Shape.of(3)),
                Tensors.seq(Shape.of(4, 1, 1, 1))
        };

        Broadcast.ElementWise result = Broadcast.elementWise(tensors);
        assertTrue(result.valid());
        assertFalse(result.unchanged());

        System.out.println("init");
        for(var layout : tensors) {
            System.out.println(layout);
        }
        System.out.println("after");
        for(var layout : result.transformed()) {
            System.out.println(layout);
        }
    }
}
