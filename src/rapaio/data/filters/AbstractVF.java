/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.filters;

import rapaio.data.Var;

/**
 * Base class for all variable filters which offers some common utility
 * methods.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public abstract class AbstractVF implements VarFilter {

    protected void checkEmptyVars(Var[] vars) {
        if (vars != null && vars.length > 0) {
            throw new IllegalArgumentException("Filter operation accepts no input variables");
        }
    }

    protected void checkSingleVar(Var[] vars) {
        if (vars == null) {
            throw new IllegalArgumentException("List of variables must not be empty");
        }
        if (vars.length != 1) {
            throw new IllegalArgumentException("Filter operation accepts a single input variable");
        }
    }

    protected void checkFixedVars(int count, Var[] vars) {
        if (count <= 0) {
            checkEmptyVars(vars);
            return;
        }
        if (vars == null || vars.length != count) {
            throw new IllegalArgumentException("Filter operation requires " + count + " input variables");
        }
    }

    protected void checkRangeVars(int min, int max, Var[] vars) {
        if (max <= 0) {
            checkEmptyVars(vars);
        }
        if (vars == null || vars.length < min || vars.length > max) {
            throw new IllegalArgumentException("Filter operation requires between " + min + " and " + max + " input variables");
        }
    }
}
