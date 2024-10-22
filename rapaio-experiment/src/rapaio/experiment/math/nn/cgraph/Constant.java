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

package rapaio.experiment.math.nn.cgraph;

import java.util.List;

import rapaio.experiment.math.nn.cgraph.operations.CompNode;
import rapaio.math.tensor.Tensor;

public class Constant extends CompNode {

    public Constant(Context c, String name, Tensor<?> value) {
        super(c, name);
        this.value = new CompValue(value);
    }

    public void assign(double value) {
    }

    @Override
    public List<CompNode> children() {
        return List.of();
    }

    @Override
    public List<Runnable> compute() {
        return List.of();
    }
}
