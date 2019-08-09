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

package rapaio.ml.common.distance;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;

import java.io.Serializable;

/**
 * Function which produces initial centroids for KMeans algorithm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/23/15.
 */
public interface KMeansInitMethod extends Serializable {

    String name();

    Frame init(Frame df, String[] inputs, int k);

    KMeansInitMethod Forgy = new KMeansInitMethod() {
        private static final long serialVersionUID = -6826959777764888621L;

        @Override
        public String name() {
            return "forgy";
        }

        @Override
        public Frame init(Frame df, String[] inputs, int k) {
            return df.mapVars(inputs).mapRows(SamplingTools.sampleWOR(df.rowCount(), k)).copy();
        }
    };
}
