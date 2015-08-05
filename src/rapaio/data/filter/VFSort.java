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

package rapaio.data.filter;

import rapaio.data.RowComparators;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFSort extends VFAbstract {

    private static final long serialVersionUID = -6260151471065618233L;
    private boolean asc;

    public VFSort() {
        this.asc = true;
    }

    public VFSort(boolean asc) {
        this.asc = asc;
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);

        switch (vars[0].getType()) {
            case NUMERIC:
            case INDEX:
            case BINARY:
            case ORDINAL:
            case STAMP:
                return new VFRefSort(RowComparators.numeric(vars[0], asc)).filter(vars);
            case NOMINAL:
            case TEXT:
                return new VFRefSort(RowComparators.nominal(vars[0], asc)).filter(vars);
        }
        return null;
    }
}
