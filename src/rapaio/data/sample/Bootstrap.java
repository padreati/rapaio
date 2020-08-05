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

package rapaio.data.sample;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;

import java.util.Objects;

import static rapaio.printer.Format.floatFlex;

/**
 * Implements bootstrap row sampling (sampling with replacement)
 *
 * By default
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/25/16.
 */
final class Bootstrap implements RowSampler {

    private static final long serialVersionUID = -7987373317949449262L;
    private final double percent;

    public Bootstrap(double percent) {
        this.percent = percent;
    }

    @Override
    public Sample nextSample(Frame df, Var weights) {
        Mapping map = Mapping.wrap(SamplingTools.sampleWR(df.rowCount(), (int) (percent * df.rowCount())));
        return new Sample(df.mapRows(map), weights.mapRows(map), map);
    }

    @Override
    public String name() {
        return "Bootstrap(p=" + floatFlex(percent) + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bootstrap bootstrap = (Bootstrap) o;
        return Double.compare(bootstrap.percent, percent) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(percent);
    }
}
