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

import java.util.List;

import rapaio.darray.Compare;
import rapaio.nn.NetState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class ELU extends AbstractNet {

    public static final double DEFAULT_ALPHA = 1;

    private final double alpha;

    public ELU(TensorManager tm) {
        this(tm, DEFAULT_ALPHA);
    }

    public ELU(TensorManager tm, double alpha) {
        super(tm);
        this.alpha = alpha;
    }

    @Override
    public List<Tensor> parameters() {
        return List.of();
    }

    @Override
    public NetState state() {
        return new NetState();
    }

    @Override
    public Tensor forward11(Tensor x) {
        var posX = x.compareTrue(Compare.GT, 0);
        var negX = x.compareFalse(Compare.GT, 0);

        return posX.add(negX.exp().sub(1).mul(alpha));
    }
}
