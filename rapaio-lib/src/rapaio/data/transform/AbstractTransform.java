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
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public abstract class AbstractTransform implements Transform {

    @Serial
    private static final long serialVersionUID = 5619103016781092137L;
    protected final VarRange varRange;
    protected String[] varNames;
    protected boolean isTrained;

    public AbstractTransform(VarRange varRange) {
        this.varRange = varRange;
    }

    @Override
    public String[] varNames() {
        return varNames;
    }

    @Override
    public final void fit(Frame df) {
        if (isTrained) {
            throw new IllegalStateException("Transformation cannot be fitted twice. Use a new unfitted instance for that purpose.");
        }
        varNames = varRange.parseVarNames(df).toArray(new String[0]);
        coreFit(df);
        isTrained = true;
    }

    protected abstract void coreFit(Frame df);

    public final Frame apply(Frame df) {
        if (!isTrained) {
            throw new IllegalStateException("Cannot apply a transformation if it is not fitted on data.");
        }
        return coreApply(df);
    }

    protected abstract Frame coreApply(Frame df);
}
