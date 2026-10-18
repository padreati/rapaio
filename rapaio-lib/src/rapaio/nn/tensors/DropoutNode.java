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

package rapaio.nn.tensors;

import java.util.Random;

import rapaio.core.distributions.Bernoulli;
import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.iterators.PointerIterator;
import rapaio.nn.Tensor;

public class DropoutNode extends Tensor {

    public DropoutNode(Tensor child, double p, Random random, boolean inplace) {
        super(child.tm(), DropoutNode.class.getSimpleName());

        DArray<?> mask = tm.zerosArray(DType.BYTE, child.value().shape());
        Bernoulli ber = Bernoulli.of(p);
        PointerIterator ptrIt = mask.ptrIterator();
        while (ptrIt.hasNext()) {
            int next = ptrIt.nextInt();
            if (!(ber.sampleNext(random) > 0.5)) {
                mask.ptrSetByte(next, (byte) 1);
            }
        }
        // Writing into the child's value is only safe when no gradient can depend on it. The backward of the node which
        // produced the child often reads its own output (sigmoid, tanh, exp) and the backward of a product reads the
        // value of the other factor, so overwriting an inner node, or a leaf which takes part in differentiation,
        // would hand them the dropped and rescaled values. In those cases the in place request falls back to a copy.
        boolean writeInPlace = inplace && child.backFunctions().isEmpty() && !child.requiresGrad();
        if (writeInPlace) {
            this.setValue(child.value().mul_(mask).div_(1 - p));
        } else {
            this.setValue(child.value().mul(mask).div_(1 - p));
        }
        // the forward scales kept units by 1/(1-p), so the backward must scale their gradient the same way
        backEdge(child, () -> this.grad().mul(mask).div_(1 - p));
    }
}
