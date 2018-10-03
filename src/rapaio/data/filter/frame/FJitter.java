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

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/10/14.
 */
public class FJitter extends AbstractFF {

    public static FJitter on(VRange vRange) {
        return new FJitter(new Normal(0, 0.1), vRange);
    }

    public static FJitter on(double sd, VRange vRange) {
        return new FJitter(new Normal(0, sd), vRange);
    }

    public static FJitter on(Distribution d, VRange vRange) {
        return new FJitter(d, vRange);
    }

    private static final long serialVersionUID = 33367007274996702L;
    private final Distribution d;

    private FJitter(Distribution d, VRange vRange) {
        super(vRange);
        this.d = d;
    }

    @Override
    public FJitter newInstance() {
        return new FJitter(d, vRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        for (String varName : varNames) {
            int varIndex = df.varIndex(varName);
            for (int i = 0; i < df.rowCount(); i++) {
                df.setDouble(i, varIndex, df.getDouble(i, varIndex) + d.sampleNext());
            }
        }
        return df;
    }
}
