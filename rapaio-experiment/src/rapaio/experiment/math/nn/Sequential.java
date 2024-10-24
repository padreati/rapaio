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

package rapaio.experiment.math.nn;

import java.util.List;

import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.Tensors;

public class Sequential extends Module {

    private final List<Module> modules;

    public Sequential(List<Module> modules) {
        this(Tensors.ofDouble(), modules);
    }

    public Sequential(TensorManager.OfType<?> tmt, List<Module> modules) {
        super(tmt);
        this.modules = modules;
    }

    @Override
    public List<Parameter> parameters() {
        return modules.stream().flatMap(module -> module.parameters().stream()).toList();
    }

    @Override
    public void bind(Context c) {
        super.bind(c);
        for (Module module : modules) {
            module.bind(c);
        }
    }

    @Override
    public Tensor<?> forward(Tensor<?> x) {
        if (modules.isEmpty()) {
            return null;
        }
        Tensor<?> value = null;
        for (Module module : modules) {
            if (value == null) {
                value = module.forward(x);
            } else {
                value = module.forward(value);
            }
        }
        return value;
    }
}
