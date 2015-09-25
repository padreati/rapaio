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

package rapaio.ml.clustering.distance;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.util.Tag;

import java.io.Serializable;

/**
 * Function which produces initial centroids for KMeans algorithm
 * <p>
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 9/23/15.
 */
public interface KMeansInitMethod extends Serializable {

    Frame init(Frame df, String[] inputs, int k);

    Tag<KMeansInitMethod> FORGY = Tag.valueOf("forgy",
            (Frame df, String[] inputs, int k) -> df.mapVars(inputs).mapRows(SamplingTools.sampleWOR(k, df.rowCount())).solidCopy());
}
