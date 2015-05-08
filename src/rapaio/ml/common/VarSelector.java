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

package rapaio.ml.common;

import rapaio.core.SamplingTool;

import java.io.Serializable;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
@Deprecated
public class VarSelector implements Serializable {
    private static final long serialVersionUID = -6800363806127327947L;

    private static final int M_ALL = 0;
    private static final int M_AUTO = -1;

    private final int mVars;
    private String[] varNames = new String[]{};

    public static VarSelector ALL = new VarSelector(M_ALL);
    public static VarSelector AUTO = new VarSelector(M_AUTO);

    public VarSelector() {
        this(M_ALL);
    }

    public VarSelector(int mVars) {
        this.mVars = mVars;
    }

    public VarSelector newInstance() {
        return new VarSelector(mVars).withVarNames(varNames);
    }

    public VarSelector withVarNames(final String... varNames) {
        this.varNames = varNames;
        return this;
    }

    public String name() {
        if (mVars == M_ALL)
            return "VarSelector[ALL]";
        if (mVars == M_AUTO)
            return "VarSelector[AUTO]";
        return "VarSelector[" + mVars + "]";
    }

    public String[] nextVarNames() {
        if (mVars == M_ALL) {
            return varNames;
        }
        int m = (mVars == M_AUTO) ? Math.max((int) Math.sqrt(varNames.length), 1) : mVars;
        int[] indexes = SamplingTool.sampleWR(m, varNames.length);
        String[] result = new String[m];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = varNames[indexes[i]];
        }
        return result;
    }
}
