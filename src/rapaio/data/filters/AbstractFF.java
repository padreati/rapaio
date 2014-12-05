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

import rapaio.data.Frame;
import rapaio.data.VarRange;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public abstract class AbstractFF implements FrameFilter {

    protected final String[] varNames;

    public AbstractFF(String[] varNames) {
        this.varNames = varNames;
    }

    protected List<String> parse(Frame df, String... varNames) {
        return new VarRange(varNames).parseVarNames(df);
    }

    protected void checkEmptyVars(Frame df, String[] varNames) {
        List<String> names = parse(df, varNames);
        if (names != null && names.size() > 0) {
            throw new IllegalArgumentException("Filter operation accepts no input variables");
        }
    }

    protected void checkSingleVar(Frame df, String[] varNames) {
        List<String> names = parse(df, varNames);
        if (names == null) {
            throw new IllegalArgumentException("List of variables must not be empty");
        }
        if (names.size() != 1) {
            throw new IllegalArgumentException("Filter operation accepts a single input variable");
        }
    }

    protected void checkFixedVars(int count, Frame df, String[] varNames) {
        List<String> names = parse(df, varNames);
        if (count <= 0) {
            checkEmptyVars(df, varNames);
            return;
        }
        if (names == null || names.size() != count) {
            throw new IllegalArgumentException("Filter operation requires " + count + " input variables");
        }
    }

    protected void checkRangeVars(int min, int max, Frame df, String... varNames) {
        List<String> names = parse(df, varNames);
        if (max <= 0) {
            checkEmptyVars(df, varNames);
        }
        if (names == null || names.size() < min || names.size() > max) {
            throw new IllegalArgumentException("Filter operation requires between " + min + " and " + max + " input variables");
        }
    }


}
