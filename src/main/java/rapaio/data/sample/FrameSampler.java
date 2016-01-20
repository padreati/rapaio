/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

import java.io.Serializable;

import static rapaio.sys.WS.formatFlex;

/**
 * Interface which defines a function which produces a data frame with a sample
 * of rows from the given data frame.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/29/15.
 */
public interface FrameSampler extends Serializable {

    /**
     * Builds a new sample from the given data frame
     *
     * @param df source data frame
     * @return new sample
     */
    FrameSample newSample(Frame df, Var weight);

    String name();

    // static implementations

    final class Identity implements FrameSampler {

        private static final long serialVersionUID = -1133893495082466752L;

        @Override
        public FrameSample newSample(Frame df, Var weights) {
            return new FrameSample(df, weights, Mapping.range(0, df.rowCount()));
        }

        @Override
        public String name() {
            return "Identity";
        }
    }

    final class Bootstrap implements FrameSampler {

        private static final long serialVersionUID = -7987373317949449262L;
        private double percent = 1.0;

        public Bootstrap(double percent) {
            this.percent = percent;
        }

        public Bootstrap withPercent(double p) {
            this.percent = p;
            return this;
        }

        @Override
        public FrameSample newSample(Frame df, Var weights) {
            Mapping map = Mapping.copy(SamplingTools.sampleWR(df.rowCount(), (int) (percent * df.rowCount())));
            return new FrameSample(df.mapRows(map), weights.mapRows(map), map);
        }

        @Override
        public String name() {
            return "Bootstrap(p=" + formatFlex(percent) + ")";
        }
    }

    final class SubSampler implements FrameSampler {

        private static final long serialVersionUID = -7987373317949449262L;
        private double percent = 1.0;

        public SubSampler(double percent) {
            this.percent = percent;
        }

        public SubSampler withPercent(double p) {
            this.percent = p;
            return this;
        }

        @Override
        public FrameSample newSample(Frame df, Var weights) {
            Mapping map = Mapping.copy(SamplingTools.sampleWOR(df.rowCount(), (int) (percent * df.rowCount())));
            return new FrameSample(df.mapRows(map), weights.mapRows(map), map);
        }

        @Override
        public String name() {
            return "SubSampler(p=" + formatFlex(percent) + ")";
        }
    }
}
