/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.FFilter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public abstract class FFDefault implements FFilter {

    private static final long serialVersionUID = 5619103016781092137L;
    protected final VRange vRange;
    protected String[] varNames;

    public FFDefault(String... varNames) {
        this(VRange.of(varNames));
    }

    public FFDefault(VRange vRange) {
        this.vRange = vRange;
    }

    @Override
    public VRange vRange() {
        return vRange;
    }

    @Override
    public String[] varNames() {
        return varNames;
    }

    protected String[] parse(Frame df) {
        varNames = vRange.parseVarNames(df).stream().toArray(String[]::new);
        return varNames;
    }

    protected void checkEmptyVars(Frame df) {
        if (varNames != null && varNames.length > 0) {
            throw new IllegalArgumentException("Filter operation accepts no input variables");
        }
    }

    protected void checkSingleVar(Frame df) {
        if (varNames == null) {
            throw new IllegalArgumentException("List of variables must not be empty");
        }
        if (varNames.length != 1) {
            throw new IllegalArgumentException("Filter operation accepts a single input variable");
        }
    }

    protected void checkFixedVars(int count, Frame df) {
        if (count <= 0) {
            checkEmptyVars(df);
            return;
        }
        if (varNames == null || varNames.length != count) {
            throw new IllegalArgumentException("Filter operation requires " + count + " input variables");
        }
    }

    protected void checkRangeVars(int min, int max, Frame df) {
        if (max <= 0) {
            checkEmptyVars(df);
        }
        if (varNames == null || varNames.length < min || varNames.length > max) {
            throw new IllegalArgumentException("Filter operation requires between " + min + " and " + max + " input variables");
        }
    }

    public void train(Frame df) {
        parse(df);
    }

}
