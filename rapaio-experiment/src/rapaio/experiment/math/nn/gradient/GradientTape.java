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

package rapaio.experiment.math.nn.gradient;

import rapaio.experiment.math.nn.DiffTensor;
import rapaio.experiment.math.nn.data.DoubleDiffTensor;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class GradientTape {

    private final AtomicInteger nameSeq = new AtomicInteger(0);
    private final Map<String, DiffTensor> nameMap = new HashMap<>();
    private final List<TapeEntry> tape = new ArrayList<>();

    public void resetTape() {
        tape.clear();
        nameMap.clear();
    }

    public List<TapeEntry> tape() {
        return tape;
    }

    public String register(String name, DiffTensor dt) {
        String key = (name == null) ? ("v" + nameSeq.getAndAdd(1)) : name;
        nameMap.put(key, dt);
        return key;
    }

    public void add(List<String> inputs, List<String> outputs, Function<List<DiffTensor>, List<DiffTensor>> propagate) {
        tape.add(new TapeEntry(inputs, outputs, propagate));
    }

    private List<DiffTensor> gather_grad(HashMap<String, DiffTensor> dL_d, List<String> entries) {
        return entries.stream().filter(dL_d::containsKey).map(dL_d::get).toList();
    }

    public List<DiffTensor> grad(DiffTensor loss, List<DiffTensor> desiredDerivatives) {

        Tensor<Double> one = Tensors.scalar(1d);

        // this map holds dL / dX for all values X
        HashMap<String, DiffTensor> dlMap = new HashMap<>();

        // It starts by initializing the 'seed' dL / dL, which is 1
        dlMap.put(loss.name(), DoubleDiffTensor.of(one, this));
        System.out.println(loss.name() + " ------------------------");

        // look up dL_dentries.If a variable is never used to compute the loss,
        // we consider its gradient None, see the note below about zeros for more information.

        // propagate the gradient information backward
        for (var entry : tape.reversed()) {
            var dOutputs = entry.outputs().stream().filter(dlMap::containsKey).map(dlMap::get).toList();
            if (dOutputs.stream().allMatch(Objects::isNull)) {
                // optimize for the case where some gradient pathways are zero.See
                // The note below for more details.
                continue;
            }

            // perform chain rule propagation specific to each compute
            var dInputs = entry.propagate().apply(dOutputs);

            // Accumulate the gradient produced for each input.
            // Each use of a variable produces some gradient dInput for that use.
            // The multivariate chain rule tells us it is safe to sum all the contributions together.
            for (int i = 0; i < entry.inputs().size(); i++) {
                var input = entry.inputs().get(i);
                var dInput = dInputs.get(i);
                if (!dlMap.containsKey(input)) {
                    dlMap.put(input, dInput);
                } else {
                    dlMap.put(input, DoubleDiffTensor.of(dlMap.get(input).asDouble().add(dInput.asDouble()), this));
                }
            }
        }

        // print some information to understand the values of each intermediate
        for (var entry : dlMap.entrySet()) {
            System.out.printf("d%s_d%s = %s%n", loss.name(), entry.getKey(), entry.getValue().name());
            System.out.println("------------------------");
        }

        return desiredDerivatives.stream()
                .filter(desired_result -> dlMap.containsKey(desired_result.name()))
                .map(desired_result -> dlMap.get(desired_result.name()))
                .toList();
    }
}
