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

package rapaio.experiment.math.cgraph;

import java.util.List;

public abstract class CNode {

    protected final CompContext c;
    protected final int id;
    protected final String name;
    public final CValue value;
    public final CValue grad;

    public CNode(CompContext c, String name) {
        this.c = c;
        this.name = name;
        this.id = c.register(this);
        this.value = new CValue();
        this.grad = new CValue();
    }

    public abstract List<CNode> children();

    public final int id() {
        return id;
    }

    public final CValue value() {
        return value;
    }

    public final CValue grad() {
        return grad;
    }

    public abstract List<Runnable> forward();

    @Override
    public final String toString() {
        return String.format("[%d] %s {val:%s, grad:%s}", id, name == null ? "" : "(" + name + ")", value, grad);
    }
}
