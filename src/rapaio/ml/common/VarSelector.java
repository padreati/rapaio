/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
import java.util.Collection;
import java.util.stream.Stream;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class VarSelector implements Serializable {

    private static final long serialVersionUID = -6800363806127327947L;

    public static VarSelector all() {
        return new VarSelector(M_ALL);
    }

    public static VarSelector auto() {
        return new VarSelector(M_AUTO);
    }

    public static VarSelector fixed(int m) {
        return new VarSelector(m);
    }

    private static final int M_ALL = 0;
    private static final int M_AUTO = -1;

    private final int mVars;
    private int mCount = 0;
    private String[] varNames = new String[0];

    private VarSelector(int mVars) {
        this.mVars = mVars;
    }

    public VarSelector newInstance() {
        return new VarSelector(mVars).withVarNames(varNames);
    }

    public VarSelector withVarNames(final String... varNames) {
        this.varNames = Arrays.stream(varNames).distinct().toArray(String[]::new);
        if (mVars == M_ALL) {
            this.mCount = this.varNames.length;
        } else if (mVars == M_AUTO) {
            this.mCount = Math.max((int) Math.ceil(Math.sqrt(this.varNames.length)), 1);
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
            return Arrays.copyOf(varNames, varNames.length);
        }
        int m = Math.min(mCount, varNames.length);
        int[] indexes = SamplingTools.sampleWOR(varNames.length, m);
        String[] result = new String[m];
        for (int i = 0; i < indexes.length; i++) {
            result[i] = varNames[indexes[i]];
        }
        return result;
    }

    public int mCount() {
        return mCount;
    }

    public void removeVarNames(Collection<String> varNames) {
        withVarNames(Arrays.stream(this.varNames).filter(vn -> !varNames.contains(vn)).distinct().toArray(String[]::new));
    }

    public void addVarNames(Collection<String> varNames) {
        withVarNames(Stream.concat(Arrays.stream(this.varNames), varNames.stream()).distinct().toArray(String[]::new));
    }
}
