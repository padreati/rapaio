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

import java.util.ArrayList;
import java.util.List;

import rapaio.math.narray.NArrayManager;
import rapaio.nn.Net;
import rapaio.nn.Tensor;
import rapaio.nn.layer.AbstractNet;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.LogSoftmax;
import rapaio.nn.layer.ReLU;

public class IrisNet extends AbstractNet {

    private final Net linear1;
    private final Net relu1;
    private final Net linear2;
    private final Net relu2;
    private final Net linear3;
    private final Net logsoftmax;

    public IrisNet(NArrayManager.OfType<?> tmt) {
        super(tmt);

        this.linear1 = new Linear(tmt.dtype(), 4, 100, true);
        this.relu1 = new ReLU();
        this.linear2 = new Linear(tmt.dtype(), 100, 20, true);
        this.relu2 = new ReLU();
        this.linear3 = new Linear(tmt.dtype(), 20, 3, true);
        this.logsoftmax = new LogSoftmax(1);
    }

    @Override
    public List<Tensor> parameters() {
        List<Tensor> params = new ArrayList<>();
        params.addAll(linear1.parameters());
        params.addAll(relu1.parameters());
        params.addAll(linear2.parameters());
        params.addAll(relu2.parameters());
        params.addAll(linear3.parameters());
        params.addAll(logsoftmax.parameters());
        return params;
    }

    @Override
    public Tensor forward11(Tensor x) {
        x = relu1.forward11(linear1.forward11(x));
        x = relu2.forward11(linear2.forward11(x));
        x = logsoftmax.forward11(linear3.forward11(x));
        return x;
    }
}
