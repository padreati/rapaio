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

import rapaio.core.SamplingTools;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class VarSelector implements Serializable {

    private static final long serialVersionUID = -6800363806127327947L;

    private static final int M_ALL = 0;
    private static final int M_AUTO = -1;
    public static VarSelector ALL = new VarSelector(M_ALL);
    public static VarSelector AUTO = new VarSelector(M_AUTO);
    private final int mVars;
    private int mCount = 0;
    private Set<String> varNames = new HashSet<>();

    public VarSelector() {
        this(M_ALL);
    }

    public VarSelector(int mVars) {
        this.mVars = mVars;
    }

    public VarSelector newInstance() {
        VarSelector sel = new VarSelector(mVars);
        sel.varNames.addAll(varNames);
        return sel;
    }

    public VarSelector withVarNames(final String... varNames) {
        this.varNames = Arrays.stream(varNames).collect(Collectors.toSet());
        if (mVars == M_ALL) {
            this.mCount = this.varNames.size();
        } else if (mVars == M_AUTO) {
            this.mCount = Math.max((int) Math.sqrt(this.varNames.size()), 1);
        } else {
            this.mCount = mVars;
        }
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
            return varNames.toArray(new String[varNames.size()]);
        }
        int m = Math.min(mCount, varNames.size());
        int[] indexes = SamplingTools.sampleWOR(varNames.size(), m);
        String[] result = new String[m];
        String[] arr = varNames.toArray(new String[varNames.size()]);
        for (int i = 0; i < indexes.length; i++) {
            result[i] = arr[indexes[i]];
        }
        return result;
    }

    public String[] nextAllVarNames() {
        int m = varNames.size();
        int[] indexes = SamplingTools.sampleWOR(varNames.size(), m);
        String[] result = new String[m];
        String[] arr = varNames.toArray(new String[varNames.size()]);
        for (int i = 0; i < indexes.length; i++) {
            result[i] = arr[indexes[i]];
        }
        return result;
    }

    public int mCount() {
        return mCount;
    }

    public void removeVarNames(List<String> varName) {
        this.varNames.removeAll(varName);
    }

    public void addVarNames(List<String> varName) {
        this.varNames.addAll(varName);
    }
}
