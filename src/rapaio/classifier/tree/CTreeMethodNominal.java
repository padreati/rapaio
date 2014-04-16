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

package rapaio.classifier.tree;

import rapaio.classifier.tools.DensityTable;
import rapaio.data.Frame;
import rapaio.data.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public enum CTreeMethodNominal {

    NONE {
        @Override
        public List<CTreeCandidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, CTreeFunction function) {
            return new ArrayList<>();
        }
    },
    FULL {
        @Override
        public List<CTreeCandidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, CTreeFunction function) {
            List<CTreeCandidate> result = new ArrayList<>();
            Vector test = df.col(testColName);
            Vector target = df.col(targetColName);

            if (new DensityTable(test, target).countWithMinimum(false, c.getMinCount()) < 2) {
                return result;
            }

            DensityTable dt = new DensityTable(test, target, df.getWeights());
            double value = function.compute(dt);
            result.add(new CTreeCandidate(value, function.sign()));
            return result;
        }
    };

    public abstract List<CTreeCandidate> computeCandidates(TreeClassifier c, Frame df, String testColName, String targetColName, CTreeFunction function);

}
