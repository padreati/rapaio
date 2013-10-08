/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package titanic;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.supervised.tree.RandomForest;

import java.io.IOException;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IrisRF {

    public static void main(String[] args) throws IOException {
        Frame iris = Datasets.loadIrisDataset();
        RandomForest rf = new RandomForest(1000, 1);
        rf.setDebug(true);
        rf.learn(iris, "class");
    }
}
