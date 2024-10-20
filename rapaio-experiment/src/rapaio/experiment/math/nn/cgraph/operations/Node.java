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

package rapaio.experiment.math.nn.cgraph.operations;

import java.util.Collections;
import java.util.List;

import rapaio.experiment.math.nn.cgraph.Context;
import rapaio.experiment.math.nn.cgraph.Value;

public abstract class Node {

    protected final Context c;
    protected final int id;
    protected final String name;
    protected Value value;
    protected Value adjoint;

    public Node(Context c, String name) {
        this.c = c;
        this.name = name;
        this.id = c.register(this, Collections.emptyList());
        this.value = new Value();
        this.adjoint = new Value();
    }

    public abstract List<Node> children();

    public final int id() {
        return id;
    }

    public final Value value() {
        return value;
    }

    public final void value(Value value) {
        this.value = value;
    }

    public final Value adjoint() {
        return adjoint;
    }

    public final void adjoint(Value value) {
        adjoint = value;
    }

    public abstract List<Runnable> compute();

    @Override
    public final String toString() {
        return String.format("[%d] %s {val:%s, adj:%s}", id, name == null ? "" : "(" + name + ")", value, adjoint);
    }
}
