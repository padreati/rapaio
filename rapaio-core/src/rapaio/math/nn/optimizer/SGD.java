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

package rapaio.math.nn.optimizer;

import java.util.Collection;

import rapaio.math.nn.Node;
import rapaio.math.nn.Optimizer;

public class SGD implements Optimizer {

    private final Collection<Node> params;
    private final double lr;

    public SGD(Collection<Node> params, double lr) {
        this.params = params;
        this.lr = lr;
    }

    @Override
    public final void zeroGrad() {
        params.forEach(Node::resetGrad);
    }

    @Override
    public void step() {
        for (var param : params) {
            param.value().sub_(param.grad().mul_(lr));
        }
    }
}
