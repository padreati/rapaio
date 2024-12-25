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

import rapaio.darray.Shape;
import rapaio.nn.Tensor;

public class StdOn extends Tensor {

    public StdOn(Tensor x, Shape shape, int ddof, double epsilon, Tensor mean) {
        super(x.tm(), StdOn.class.getSimpleName());

        double dof = x.size() - ddof;
        var mu = mean != null ? mean : x.meanOn(shape);
        var centered = x.value().sub(mu.value());
        var std = x.value().varOn(shape, ddof, true, mu.value()).add_(epsilon).sqrt_();
        this.setValue(std);
        backEdge(x, () -> this.grad().mul(centered.div(std).div_(dof)));
        backEdge(mu, () -> tm.zerosTensor(mu.shape()).value());
    }
}
