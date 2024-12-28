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

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class RemoveVars extends AbstractTransform {

    public static RemoveVars remove(VarRange varRange) {
        return new RemoveVars(varRange);
    }

    @Serial
    private static final long serialVersionUID = -932131127278719356L;

    private RemoveVars(VarRange varRange) {
        super(varRange);
    }

    @Override
    public RemoveVars newInstance() {
        return new RemoveVars(varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        return df.removeVars(VarRange.of(varNames));
    }
}
