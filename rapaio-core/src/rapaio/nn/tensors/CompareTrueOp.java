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

import rapaio.math.narray.NArray;
import rapaio.math.narray.Compare;
import rapaio.nn.Tensor;

public class CompareTrueOp extends AbstractTensor {

    private final Tensor x;
    private final Compare compare;
    private final double threshold;

    private NArray<?> mask;

    public CompareTrueOp(Tensor x, Compare cmp, double threshold) {
        super(x.dtype(), "cmpTrue");
        this.x = x;
        this.compare = cmp;
        this.threshold = threshold;

        forward();
    }

    public NArray<?> mask() {
        return mask;
    }

    private void forward() {
        this.mask = x.value().copy().compareMask_(compare, threshold);
        this.setValue(mask.mul(x.value()));
        backEdge(x, () -> this.grad().mul(mask));
    }
}
