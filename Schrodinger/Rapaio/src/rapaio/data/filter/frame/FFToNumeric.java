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

package rapaio.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.Var;
import rapaio.data.filter.var.VFToNumeric;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Convert to numeric values all the selected variables which are nominal.
 * All the other columns remain the same.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFToNumeric extends AbstractFF {

    private static final long serialVersionUID = -6745637493367588453L;

    public FFToNumeric(VRange vRange) {
        super(vRange);
    }

    @Override
    public FFToNumeric newInstance() {
        return new FFToNumeric(vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        checkRangeVars(1, df.varCount(), df);

        Set<String> nameSet = Arrays.stream(varNames).collect(Collectors.toSet());
        Var[] vars = new Var[df.varCount()];
        for (int i = 0; i < vars.length; i++) {
            if (!nameSet.contains(df.rvar(i).name())) {
                vars[i] = df.rvar(i);
            } else {
                vars[i] = VFToNumeric.byDefault().fitApply(df.rvar(i));
            }
        }
        return SolidFrame.byVars(df.rowCount(), vars);
    }
}
