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
import rapaio.data.SolidFrame;
import rapaio.data.Var;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convert to numeric values all the selected variables which are nominal.
 * All the other columns remain the same.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFToNumeric extends AbstractFF {

    private Set<String> nameSet = new HashSet<>();

    @Override
    public void fit(Frame df, String... varNames) {
        checkRangeVars(1, df.varCount(), df, varNames);
        List<String> names = parse(df, varNames);
        nameSet = new HashSet<>(names);
    }

    @Override
    public Frame apply(Frame df) {

        Var[] vars = new Var[df.varCount()];
        for (int i = 0; i < vars.length; i++) {
            if (nameSet.contains(df.var(i).name())) {
                vars[i] = df.var(i);
            } else {
                vars[i] = new VFToNumeric().fitApply(df.var(i));
            }
        }
        return SolidFrame.newWrapOf(df.rowCount(), vars);
    }
}
