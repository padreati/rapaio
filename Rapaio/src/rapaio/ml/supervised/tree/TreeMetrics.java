/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.ml.supervised.tree;

import rapaio.data.Frame;
import rapaio.filters.FilterGroupByNominal;

import static rapaio.core.BaseMath.log;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class TreeMetrics {

    public double entropy(Frame df, int classIndex) {
        int[] hits = new int[df.getCol(classIndex).dictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            hits[df.getCol(classIndex).getIndex(i)]++;
        }
        double entropy = 0.;
        for (int i = 0; i < hits.length; i++) {
            if (hits[i] != 0) {
                double p = hits[i] / (1. * df.getRowCount());
                entropy += -p * log(p) / log(2);
            }
        }
        return entropy;
    }

    public double infoGain(Frame df, int ClassIndex, int splitIndex) {
        Frame[] split = new FilterGroupByNominal().groupByNominal(df, splitIndex);
        double infoGain = entropy(df, ClassIndex);
        for (Frame f : split) {
            if (f == null) {
                continue;
            }
            infoGain -= (f.getRowCount() / (1. * df.getRowCount())) * entropy(f, ClassIndex);
        }
        return infoGain;
    }
}
