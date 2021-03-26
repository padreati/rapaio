/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.data.filter;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public abstract class AbstractFFilter implements FFilter {

    private static final long serialVersionUID = 5619103016781092137L;
    protected final VarRange varRange;
    protected String[] varNames;

    public AbstractFFilter(VarRange varRange) {
        this.varRange = varRange;
    }

    @Override
    public String[] varNames() {
        return varNames;
    }

    @Override
    public void fit(Frame df) {
        varNames = varRange.parseVarNames(df).toArray(new String[0]);
        coreFit(df);
    }

    protected abstract void coreFit(Frame df);
}
