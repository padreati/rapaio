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
import java.util.Random;

import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;

public abstract class Module {

    protected Context c;
    protected Random random = new Random();
    protected final TensorManager.OfType<?> tmt;

    public Module(TensorManager.OfType<?> tmt) {
        this.tmt = tmt;
    }

    public final Random getRandom() {
        return random;
    }

    public final void seed(long seed) {
        random = new Random(seed);
    }

    public abstract List<Parameter> parameters();

    public abstract Tensor<?> forward(Tensor<?> x);

    public void bind(Context c) {
        this.c = c;
    }
}
