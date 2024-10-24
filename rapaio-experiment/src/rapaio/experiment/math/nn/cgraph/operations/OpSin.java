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

package rapaio.experiment.math.nn.cgraph.operations;

import java.util.List;

import rapaio.experiment.math.nn.cgraph.CompContext;

public class OpSin extends CompNode {

    private final CompNode child;

    public OpSin(CompContext c, CompNode child) {
        super(c, "sin");
        this.child = child;
    }

    @Override
    public List<CompNode> children() {
        return List.of(child);
    }

    @Override
    public List<Runnable> compute() {
        value.assign(child.value.tensor().sin());
        return List.of(() -> child.adjoint.add_(this.adjoint.tensor().mul(child.value.tensor().cos())));
    }
}
