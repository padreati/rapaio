/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import rapaio.data.Frame;
import rapaio.data.VarRange;

import java.io.Serial;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FRemoveVars extends AbstractFFilter {

    public static FRemoveVars remove(VarRange varRange) {
        return new FRemoveVars(varRange);
    }

    @Serial
    private static final long serialVersionUID = -932131127278719356L;

    private FRemoveVars(VarRange varRange) {
        super(varRange);
    }

    @Override
    public FRemoveVars newInstance() {
        return new FRemoveVars(varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        return df.removeVars(VarRange.of(varNames));
    }
}
