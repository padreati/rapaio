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

package rapaio.nn.layer;

import java.util.Random;

import rapaio.nn.Net;
import rapaio.nn.TensorManager;

public abstract class AbstractNet implements Net {

    protected final Random random = new Random();
    protected final TensorManager tm;
    protected boolean train = false;

    public AbstractNet(TensorManager tm) {
        this.tm = tm;
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    @Override
    public void seed(long seed) {
        random.setSeed(seed);
    }

    @Override
    public Random random() {
        return random;
    }

    @Override
    public void train() {
        train = true;
    }

    @Override
    public void eval() {
        train = false;
    }
}
