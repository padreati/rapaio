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
import rapaio.data.VRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class FFRemoveVars extends AbstractFF {

    private static final long serialVersionUID = -932131127278719356L;

    public FFRemoveVars(VRange vRange) {
        super(vRange);
    }

    @Override
    public FFRemoveVars newInstance() {
        return new FFRemoveVars(vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        checkRangeVars(0, df.varCount(), df);
        return df.removeVars(VRange.of(varNames));
    }
}
