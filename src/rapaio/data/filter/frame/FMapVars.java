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

package rapaio.data.filter.frame;

import rapaio.data.*;

/**
 * Filter to map vars from a data frame.
 * <p>
 * Created by padreati on 1/15/16.
 */
public class FMapVars extends AbstractFF {

    public static FMapVars map(VRange vRange) {
        return new FMapVars(vRange);
    }

    private static final long serialVersionUID = 5540246008233767364L;

    private FMapVars(VRange vRange) {
        super(vRange);
    }

    @Override
    public FMapVars newInstance() {
        return new FMapVars(vRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        return df.mapVars(varNames);
    }
}
