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

package org.jupyter.extension.table;

import org.rapaio.jupyter.kernel.display.DisplayTransformer;
import org.rapaio.jupyter.kernel.display.table.TableDisplay;
import org.rapaio.jupyter.kernel.display.table.TableDisplayWrapper;

import rapaio.data.Var;

public class VarTableDisplayTransformer implements DisplayTransformer {

    @Override
    public boolean canTransform(Object o) {
        return o instanceof Var;
    }

    @Override
    public Class<?> transformedClass() {
        return TableDisplay.class;
    }

    @Override
    public Object transform(Object o) {
        if (!canTransform(o)) {
            throw new IllegalArgumentException("Can't transform object: " + o);
        }
        Var v = (Var) o;
        return new TableDisplayWrapper().withColumn(
                v.name(),
                FrameDisplayTransformer.fromType(v.type()),
                v.size(),
                i -> v.isMissing(i) ? null : v.getLabel(i)
        );
    }
}
