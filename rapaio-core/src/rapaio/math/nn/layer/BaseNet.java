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

package rapaio.math.nn.layer;

import java.util.Random;

import rapaio.math.nn.Net;
import rapaio.math.nn.Tensor;
import rapaio.math.narrays.NArrayManager;
import rapaio.util.NotImplementedException;

public abstract class BaseNet implements Net {

    protected final Random random = new Random();
    protected final NArrayManager.OfType<?> tmt;
    protected boolean train = false;

    public BaseNet(NArrayManager.OfType<?> tmt) {
        this.tmt = tmt;
    }

    @Override
    public void seed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public void train() {
        train = true;
    }

    @Override
    public void eval() {
        train = false;
    }

    @Override
    public Tensor[] forward(Tensor... xs) {
        if (xs.length != 1) {
            throw new IllegalArgumentException("xs.length != 1");
        }
        return new Tensor[] {forward11(xs[0])};
    }

    protected Tensor forward11(Tensor x) {
        throw new NotImplementedException();
    }
}
