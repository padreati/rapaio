/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
import java.util.Random;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/10/14.
 */
public class Jitter extends AbstractTransform {

    public static Jitter on(VarRange varRange) {
        return on(new Random(), Normal.of(0, 0.1), varRange);
    }

    public static Jitter on(Random random, VarRange varRange) {
        return new Jitter(random, Normal.of(0, 0.1), varRange);
    }

    public static Jitter on(double sd, VarRange varRange) {
        return on(new Random(), Normal.of(0, sd), varRange);
    }

    public static Jitter on(Random random, double sd, VarRange varRange) {
        return new Jitter(random, Normal.of(0, sd), varRange);
    }

    public static Jitter on(Distribution d, VarRange varRange) {
        return new Jitter(new Random(), d, varRange);
    }

    public static Jitter on(Random random, Distribution d, VarRange varRange) {
        return new Jitter(random, d, varRange);
    }

    @Serial
    private static final long serialVersionUID = 33367007274996702L;
    private final Random random;
    private final Distribution d;

    private Jitter(Random random, Distribution d, VarRange varRange) {
        super(varRange);
        this.random = random;
        this.d = d;
    }

    @Override
    public Jitter newInstance() {
        return new Jitter(random, d, varRange);
    }

    @Override
    protected void coreFit(Frame df) {
    }

    @Override
    public Frame coreApply(Frame df) {
        for (String varName : varNames) {
            int varIndex = df.varIndex(varName);
            for (int i = 0; i < df.rowCount(); i++) {
                df.setDouble(i, varIndex, df.getDouble(i, varIndex) + d.sampleNext(random));
            }
        }
        return df;
    }
}
