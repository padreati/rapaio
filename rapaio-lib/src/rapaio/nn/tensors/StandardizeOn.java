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

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.Tensor;

public class StandardizeOn extends Tensor {

    private final DArray<?> mean;
    private final DArray<?> std;

    public StandardizeOn(Tensor x, Shape shape, int ddof, double epsilon) {
        super(x.tm(), Standardize1d.class.getSimpleName());

        DArray<?> vx = x.value();
        mean = vx.meanOn(shape, true);
        std = x.value().varOn(shape, ddof, true, mean).add_(epsilon).sqrt_();

        DArray<?> vs = vx.sub(mean).div_(std);

        this.setValue(vs);

        backEdge(x, () -> {
            DArray<?> ds = this.grad;
            var dsSum = ds.sumOn(shape, true);
            var dssSum = ds.mul(vs).sumOn(shape, true);

            var t1 = ds.div(std);
            var t2 = dsSum.add(vs.mul(dssSum)).div(shape.size()).div(std);

            return t1.sub_(t2);
        });
    }

    public Tensor outputMean() {
        return tm.var(mean);
    }

    public Tensor outputStd() {
        return tm.var(std);
    }
}
