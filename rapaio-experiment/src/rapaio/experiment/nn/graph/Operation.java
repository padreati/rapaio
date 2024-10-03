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

package rapaio.experiment.nn.graph;

import java.util.ArrayList;
import java.util.List;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;

public abstract class Operation extends Node {

    private final List<Node> inputs;

    protected Operation(Graph graph, String name, List<Node> inputs) {
        super(graph, name);
        this.inputs = new ArrayList<>(inputs);
        for (var input : inputs) {
            input.consumers().add(this);
        }
    }

    public List<Node> inputs() {
        return inputs;
    }

    public abstract Tensor<?> compute(List<? extends Tensor<?>> operands);

    protected boolean checkAllDouble(List<? extends Tensor<?>> operands) {
        for (var op : operands) {
            if (op.dtype() != DType.DOUBLE) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkAllFloat(List<? extends Tensor<?>> operands) {
        for (var op : operands) {
            if (op.dtype() != DType.FLOAT) {
                return false;
            }
        }
        return true;
    }

    protected boolean checkAllInt(List<? extends Tensor<?>> operands) {
        for (var op : operands) {
            if (op.dtype() != DType.INTEGER) {
                return false;
            }
        }
        return true;
    }
}
