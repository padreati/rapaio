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
import java.util.stream.Collectors;

public final class ModulePipe {

    public static ModulePipe newPipe() {
        return new ModulePipe(List.of(), List.of());
    }

    public static ModulePipe newStartPipe(List<Module> outputs) {
        return new ModulePipe(List.of(), outputs);
    }

    public static ModulePipe newEndPipe(List<Module> inputs) {
        return new ModulePipe(inputs, List.of());
    }

    private final List<Module> inputs;
    private final List<Module> outputs;

    private ModulePipe(List<Module> inputs, List<Module> outputs) {
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>(outputs);
    }

    public List<Module> getInputs() {
        return inputs;
    }

    public List<Module> getOutputs() {
        return outputs;
    }

    public void setInputs(List<Module> modules) {
        if (!inputs.isEmpty()) {
            throw new IllegalArgumentException();
        }
        inputs.addAll(modules);
    }

    public void setOutputs(List<Module> modules) {
        if (!outputs.isEmpty()) {
            throw new IllegalArgumentException();
        }
        outputs.addAll(modules);
    }

    @Override
    public String toString() {
        return String.format("ModulePipe{inputs:[%s],outputs:[%s]}",
                inputs.stream().map(Module::name).collect(Collectors.joining(",")),
                outputs.stream().map(Module::name).collect(Collectors.joining(",")));
    }
}
