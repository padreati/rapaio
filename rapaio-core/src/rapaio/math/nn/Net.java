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

package rapaio.math.nn;

import java.util.List;
import java.util.Random;

import rapaio.math.tensor.TensorManager;
import rapaio.util.NotImplementedException;

public abstract class Net {

    protected final Random random = new Random();
    protected final TensorManager.OfType<?> tmt;
    protected boolean train = false;

    public Net(TensorManager.OfType<?> tmt) {
        this.tmt = tmt;
    }

    public final Random getRandom() {
        return random;
    }

    public void seed(long seed) {
        random.setSeed(seed);
    }

    public abstract List<Node> parameters();

    public void train() {
        train = true;
    }

    public void eval() {
        train = false;
    }

    public Node[] forward(Node... xs) {
        if (xs.length != 1) {
            throw new IllegalArgumentException("xs.length != 1");
        }
        return new Node[] {forward11(xs[0])};
    }

    protected Node forward11(Node x) {
        throw new NotImplementedException();
    }
}
