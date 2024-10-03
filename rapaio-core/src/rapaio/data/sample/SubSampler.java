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

package rapaio.data.sample;

import static rapaio.printer.Format.*;

import java.io.Serial;
import java.util.Objects;
import java.util.Random;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

/**
 * Sample obtaind by subsampling, which is a sample without replacement
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
record SubSampler(double percent) implements RowSampler {

    @Serial
    private static final long serialVersionUID = -7987373317949449262L;

    @Override
    public Sample nextSample(final Random random, final Frame df, final Var weights) {
        Mapping map = Mapping.wrap(SamplingTools.sampleWOR(random, df.rowCount(), (int) (percent * df.rowCount())));
        return new Sample(df.mapRows(map), weights.mapRows(map), map, df.rowCount());
    }

    @Override
    public String name() {
        return "SubSampler(p=" + floatFlex(percent) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubSampler that = (SubSampler) o;
        return Double.compare(that.percent, percent) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percent);
    }
}
