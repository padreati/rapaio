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

import rapaio.printer.Printable;

public abstract class Module implements Printable {

    protected final List<Module> inputs = new ArrayList<>();
    protected final List<Module> outputs = new ArrayList<>();
    protected final String name;
    protected boolean compiled;

    public Module(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public List<Module> getInputs() {
        return inputs;
    }

    public List<Module> getOutputs() {
        return outputs;
    }

    public void compile() {
        if (!compiled) {
            // TODO
        }
    }

    public abstract void bindAfter(List<Module> modules);

    public abstract void bindBefore(List<Module> modules);

    public abstract List<? extends DiffTensor> forward(List<? extends DiffTensor> inputValues);

//    protected List<Module> topologicalSort() {
//        List<Module> modules = new LinkedList<>();
//        Set<Module> visited = new HashSet<>();
//        Queue<Module> queue = new LinkedList<>(List.of(this));
//        while(!queue.isEmpty()) {
//            Module module = queue.poll();
//            if(visited.contains(module)) {
//                continue;
//            }
//            visited.add(module);
//            modules.add(module);
//            queue.addAll(module.consumers());
//            queue.addAll(module.submodules());
//        }
//        return modules;
//    }
//
//    @Override
//    public String toSummary(Printer printer, POpt<?>... options) {
//        // TODO finalize
//        StringBuilder sb = new StringBuilder();
//        List<Module> modules = topologicalSort();
//        for(Module module : modules) {
//            sb.append(STR."Module name: \{module.name}, type: \{module.getClass().getSimpleName()}\n");
//            sb.append(STR." - \{module.inputs().size()} inputs: [\{module.inputs().stream().map(Module::name).collect(Collectors.joining(","))}]\n");
//            sb.append(STR." - \{module.consumers().size()} consumers: [\{module.consumers().stream().map(Module::name).collect(Collectors.joining(","))}]\n");
//            sb.append("\n");
//        }
//        return sb.toString();
//    }

    @Override
    public String toString() {
        return "Module %s, type: %s".formatted(name, getClass().getSimpleName());
    }
}
