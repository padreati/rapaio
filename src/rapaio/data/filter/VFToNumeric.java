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

import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.VarType;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFToNumeric extends VFAbstract {

    private static final long serialVersionUID = -6471901421507667237L;

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        Var v = vars[0];
        if (v.type().equals(VarType.NUMERIC)) {
            return v;
        }
        final Numeric result = Numeric.newEmpty();
        v.stream().forEach(s -> {
            if (s.missing()) {
                result.addMissing();
            } else {
                switch (v.type()) {
                    case NOMINAL:
                        try {
                            double value = Double.parseDouble(s.label());
                            result.addValue(value);
                        } catch (NumberFormatException nfe) {
                            result.addMissing();
                        }
                        break;
                    case INDEX:
                        result.addValue(s.index());
                        break;
                }
            }
        });
        return result;
    }
}
