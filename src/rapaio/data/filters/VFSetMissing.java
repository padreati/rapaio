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

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/4/14.
 */
public class VFSetMissing extends AbstractVF {

    private final Collection<String> missingValues;

    public VFSetMissing(Collection<String> missingValues) {
        this.missingValues = missingValues;
    }

    public VFSetMissing(String... missingValues) {
        this.missingValues = Arrays.asList(missingValues);
    }

    @Override
    public void fit(Var... vars) {
        checkSingleVar(vars);
    }

    @Override
    public Var apply(Var... vars) {
        checkSingleVar(vars);
        vars[0].stream().forEach(s -> {
            if (missingValues.contains(s.label()))
                s.setMissing();
        });
        return vars[0];
    }
}
