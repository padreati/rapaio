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

package rapaio.data.filter.frame;

import rapaio.data.Frame;
import rapaio.data.VRange;

/**
 * Filter to map vars from a data frame.
 * <p>
 * Created by padreati on 1/15/16.
 */
public class FFMapVars extends FFDefault {

    private static final long serialVersionUID = 5540246008233767364L;

    public FFMapVars(VRange vRange) {
        super(vRange);
    }

    @Override
    public FFMapVars newInstance() {
        return new FFMapVars(vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
    }

    @Override
    public Frame apply(Frame df) {
        checkRangeVars(0, df.varCount(), df);
        return df.mapVars(varNames);
    }
}
