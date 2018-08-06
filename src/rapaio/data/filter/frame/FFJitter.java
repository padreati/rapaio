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

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/10/14.
 */
public class FFJitter extends AbstractFF {

    private static final long serialVersionUID = 33367007274996702L;

    private final Distribution d;

    public FFJitter(VRange vRange) {
        this(0.1, vRange);
    }

    public FFJitter(double sd, VRange vRange) {
        this(new Normal(0, sd), vRange);
    }

    public FFJitter(Distribution d, VRange vRange) {
        super(vRange);
        if (d == null) {
            throw new IllegalArgumentException("distribution parameter cannot be empty");
        }
        this.d = d;
    }

    @Override
    public FFJitter newInstance() {
        return new FFJitter(d, vRange);
    }

    @Override
    public void train(Frame df) {
        parse(df);
        checkRangeVars(1, df.varCount(), df);
    }

    @Override
    public Frame apply(Frame df) {
        for (int i = 0; i < df.rowCount(); i++) {
            for (String varName : varNames) {
                df.setDouble(i, varName, df.getDouble(i, varName) + d.sampleNext());
            }
        }
        return df;
    }
}
