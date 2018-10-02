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

import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.filter.var.VTransformBoxCox;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class FFBoxCoxT extends AbstractFF {

    private static final long serialVersionUID = 1804199711139024129L;

    private final VTransformBoxCox bct;

    public FFBoxCoxT(double lambda, String... varNames) {
        this(lambda, 0, VRange.of(varNames));
    }

    public FFBoxCoxT(double lambda, VRange vRange) {
        this(lambda, 0, vRange);
    }

    public FFBoxCoxT(double lambda, double shift, String... varNames) {
        super(VRange.of(varNames));
        this.bct = VTransformBoxCox.with(lambda, shift);
    }

    public FFBoxCoxT(double lambda, double shift, VRange vRange) {
        super(vRange);
        this.bct = VTransformBoxCox.with(lambda, shift);
    }

    @Override
    public FFBoxCoxT newInstance() {
        return new FFBoxCoxT(bct.lambda(), bct.shift(), vRange);
    }

    @Override
    public void fit(Frame df) {
        parse(df);
        checkRangeVars(0, df.varCount(), df);
    }

    @Override
    public Frame apply(Frame df) {
        String[] names = parse(df);
        for (String name : names) {
            bct.fapply(df.rvar(name));
        }
        return df;
    }
}
