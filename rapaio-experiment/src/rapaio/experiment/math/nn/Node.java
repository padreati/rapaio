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

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Tensor;
import rapaio.printer.Format;

public abstract class Node {
    protected String name;
    private final DType<?> dtype;
    private Tensor<?> value;
    private Tensor<?> adjoint;

    protected Node(DType<?> dtype, String name) {
        this.dtype = dtype;
        this.name = name;
    }

    public DType<?> dtype() {
        return dtype;
    }

    public String name() {
        return name;
    }

    public Node name(String name) {
        this.name = name;
        return this;
    }

    public abstract List<Node> children();

    public final Tensor<?> value() {
        return value;
    }

    public final void value(Tensor<?> value) {
        this.value = value;
    }

    public final Tensor<?> adjoint() {
        return adjoint;
    }

    public final void adjoint(Tensor<?> value) {
        adjoint = value;
    }

    public void adjointAdd_(Tensor<?> other) {
        if (adjoint==null) {
            adjoint = other;
        } else {
            adjoint.add_(other);
        }
    }

    public void adjointSub_(Tensor<?> other) {
        if (adjoint==null) {
            adjoint = other.neg();
        } else {
            adjoint.sub_(other);
        }
    }

    public void resetAdjoint() {
        this.adjoint = null;
    }

    public abstract List<Runnable> forward();

    @Override
    public final String toString() {
        return String.format("%s {\nval:%s, adj:%s}", name == null ? "" : "(" + name + ")", format(value), format(adjoint));
    }

    private String format(Tensor<?> tensor) {
        if(tensor==null) {
            return "NaN";
        }
        if(tensor.isScalar()) {
            return Format.floatFlex(tensor.getDouble());
        }
        return tensor.toString();
    }
}
