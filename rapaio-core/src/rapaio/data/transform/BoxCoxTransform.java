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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.data.transform;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class BoxCoxTransform extends AbstractTransform {

    public static BoxCoxTransform on(double lambda, double shift, String... varNames) {
        return new BoxCoxTransform(lambda, shift, VarRange.of(varNames));
    }

    public static BoxCoxTransform on(double lambda, double shift, VarRange varRange) {
        return new BoxCoxTransform(lambda, shift, varRange);
    }

    @Serial
    private static final long serialVersionUID = 1804199711139024129L;
    private final VarBoxCoxTransform bct;

    private BoxCoxTransform(double lambda, double shift, VarRange varRange) {
        super(varRange);
        this.bct = VarBoxCoxTransform.with(lambda, shift);
    }

    @Override
    public BoxCoxTransform newInstance() {
        return new BoxCoxTransform(bct.lambda(), bct.shift(), varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        for (String name : varNames) {
            bct.fapply(df.rvar(name));
        }
        return df;
    }
}
