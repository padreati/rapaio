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

package rapaio.data.transform;

import java.io.Serial;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;

/**
 * Convert to numeric double values all the selected variables which are nominal.
 * All the other columns remain the same.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class ToDoubleTransform extends AbstractTransform {

    public static ToDoubleTransform on(VarRange varRange) {
        return new ToDoubleTransform(varRange);
    }

    @Serial
    private static final long serialVersionUID = -6745637493367588453L;

    private ToDoubleTransform(VarRange varRange) {
        super(varRange);
    }

    @Override
    public ToDoubleTransform newInstance() {
        return new ToDoubleTransform(varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        Set<String> nameSet = Arrays.stream(varNames).collect(Collectors.toSet());
        Var[] vars = new Var[df.varCount()];
        for (int i = 0; i < vars.length; i++) {
            if (!nameSet.contains(df.rvar(i).name())) {
                vars[i] = df.rvar(i);
            } else {
                vars[i] = VarToDoubleTransform.byDefault().fapply(df.rvar(i));
            }
        }
        return SolidFrame.byVars(df.rowCount(), vars);
    }
}
