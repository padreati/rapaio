/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/15/14.
 */
public class FTransformBoxCox extends AbstractFFilter {

    public static FTransformBoxCox on(double lambda, double shift, String... varNames) {
        return new FTransformBoxCox(lambda, shift, VarRange.of(varNames));
    }

    public static FTransformBoxCox on(double lambda, double shift, VarRange varRange) {
        return new FTransformBoxCox(lambda, shift, varRange);
    }

    @Serial
    private static final long serialVersionUID = 1804199711139024129L;
    private final VTransformBoxCox bct;

    private FTransformBoxCox(double lambda, double shift, VarRange varRange) {
        super(varRange);
        this.bct = VTransformBoxCox.with(lambda, shift);
    }

    @Override
    public FTransformBoxCox newInstance() {
        return new FTransformBoxCox(bct.lambda(), bct.shift(), varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame apply(Frame df) {
        for (String name : varNames) {
            bct.fapply(df.rvar(name));
        }
        return df;
    }
}
