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

import java.util.List;

import rapaio.experiment.math.nn.cgraph.CompContext;
import rapaio.experiment.math.nn.cgraph.CompValue;

public abstract class CompNode {

    protected final CompContext c;
    protected final int id;
    protected final String name;
    protected CompValue value;
    protected CompValue adjoint;

    public CompNode(CompContext c, String name) {
        this.c = c;
        this.name = name;
        this.id = c.register(this);
        this.value = new CompValue();
        this.adjoint = new CompValue();
    }

    public abstract List<CompNode> children();

    public final int id() {
        return id;
    }

    public final CompValue value() {
        return value;
    }

    public final void value(CompValue value) {
        this.value = value;
    }

    public final CompValue adjoint() {
        return adjoint;
    }

    public final void adjoint(CompValue value) {
        adjoint = value;
    }

    public abstract List<Runnable> compute();

    @Override
    public final String toString() {
        return String.format("[%d] %s {\nval:%s, adj:%s}", id, name == null ? "" : "(" + name + ")", value, adjoint);
    }
}
