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

package rapaio.sandbox;

import rapaio.core.sample.Sampler;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.ModelEvaluation;
import rapaio.ml.classifier.tree.CForest;

import java.io.IOException;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class LifeScience {

    public static void main(String[] args) throws IOException {

        Frame df = Datasets.loadLifeScience();
        new ModelEvaluation().cv(df, "class", CForest.newRF(4, new Sampler.Bootstrap(0.9)), 10);
    }
}
