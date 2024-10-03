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

package rapaio.math.tensor.operator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

public class BroadcastTest {

    @Test
    void testElementWiseValidation() {
        List<Tensor<?>> tensors = List.of(
                Tensors.seq(Shape.of(2, 1)),
                Tensors.seq(Shape.of(3, 2, 3)),
                Tensors.seq(Shape.of(3))
        );

        Broadcast.ElementWise result = Broadcast.elementWise(tensors);
        assertTrue(result.valid());
        assertFalse(result.unchanged());

        System.out.println("init");
        for (var t : tensors) {
            System.out.print(t);
        }
        System.out.println(result);
        System.out.println("after");
        for (var t : tensors) {
            System.out.print(result.transform(t));
        }
    }
}
