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

package rapaio.nn.tensors;

import java.util.Random;

import rapaio.core.distributions.Bernoulli;
import rapaio.nn.Tensor;
import rapaio.narray.NArray;
import rapaio.narray.NArrays;
import rapaio.narray.iterators.PointerIterator;

public class DropoutOp extends AbstractTensor {

    private final Tensor child;
    private final double p;
    private NArray<Byte> mask;
    private final Random random;
    private final boolean inplace;

    public DropoutOp(Tensor child, double p, Random random, boolean inplace) {
        super(child.tm(), "identity");
        this.child = child;
        this.p = p;
        this.random = random;
        this.inplace = inplace;
        forward();
    }

    public void forward() {
        this.mask = NArrays.ofByte().zeros(child.value().shape());
        Bernoulli ber = Bernoulli.of(p);
        PointerIterator ptrIt = mask.ptrIterator();
        while (ptrIt.hasNext()) {
            int next = ptrIt.nextInt();
            if (!(ber.sampleNext(random) > 0.5)) {
                mask.ptrSetByte(next, (byte) 1);
            }
        }
        if (inplace) {
            this.setValue(child.value().mul_(mask).div_(1 - p));
        } else {
            this.setValue(child.value().mul(mask).div_(1 - p));
        }
        backEdge(child, () -> this.grad().mul(mask));
    }
}
