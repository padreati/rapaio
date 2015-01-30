/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.sample;

import rapaio.core.Printable;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.printer.Printer;

/**
 * Interface which defines a function which produces a data frame with a sample
 * of rows from the given data frame.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/29/15.
 */
public interface Sampler extends Printable {

    /**
     * Builds a new sample from the given data frame
     *
     * @param df source data frame
     * @return new sample
     */
    Sample newSample(Frame df, Var weight);

    default void buildSummary(StringBuilder sb) {
        sb.append("NotImplemented");
    }

    // static implementations

    public static class Identity implements Sampler {

        @Override
        public Sample newSample(Frame df, Var weights) {
            return new Sample(df, weights, Mapping.newRangeOf(0, df.rowCount()));
        }

        @Override
        public void buildSummary(StringBuilder sb) {
            sb.append("Sampler.Identity\n");
        }
    }

    public static final class Bootstrap implements Sampler {

        private double percent = 1.0;

        public Bootstrap withPercent(double p) {
            this.percent = p;
            return this;
        }

        @Override
        public Sample newSample(Frame df, Var weights) {
            Mapping map = Mapping.newCopyOf(SamplingTool.sampleWR(df.rowCount(), (int) (percent * df.rowCount())));
            return new Sample(df.mapRows(map), weights.mapRows(map), map);
        }

        @Override
        public void buildSummary(StringBuilder sb) {
            sb.append("Sampler.Bootstrap{p=").append(Printer.formatDecShort.format(percent)).append("}\n");
        }
    }
}
