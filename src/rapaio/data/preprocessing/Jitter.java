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

package rapaio.data.preprocessing;

import java.io.Serial;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/10/14.
 */
public class Jitter extends AbstractTransform {

    public static Jitter on(VarRange varRange) {
        return new Jitter(Normal.of(0, 0.1), varRange);
    }

    public static Jitter on(double sd, VarRange varRange) {
        return new Jitter(Normal.of(0, sd), varRange);
    }

    public static Jitter on(Distribution d, VarRange varRange) {
        return new Jitter(d, varRange);
    }

    @Serial
    private static final long serialVersionUID = 33367007274996702L;
    private final Distribution d;

    private Jitter(Distribution d, VarRange varRange) {
        super(varRange);
        this.d = d;
    }

    @Override
    public Jitter newInstance() {
        return new Jitter(d, varRange);
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
