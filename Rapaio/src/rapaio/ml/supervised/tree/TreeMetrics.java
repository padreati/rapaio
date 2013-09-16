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

package rapaio.ml.supervised.tree;

import static rapaio.core.BaseMath.log;
import rapaio.data.Frame;
import rapaio.filters.NominalFilters;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class TreeMetrics {

    public double entropy(Frame df, String classColName) {
        int classIndex = df.getColIndex(classColName);
        int[] hits = new int[df.getCol(classIndex).getDictionary().length];
        for (int i = 0; i < df.getRowCount(); i++) {
            hits[df.getCol(classIndex).getIndex(i)]++;
        }
        double entropy = 0.;
        for (int hit : hits) {
            if (hit != 0) {
                double p = hit / (1. * df.getRowCount());
                entropy += -p * log(p) / log(2);
            }
        }
        return entropy;
    }

    public double entropy(Frame df, String classColName, String splitColName) {
        int splitIndex = df.getColIndex(splitColName);
        Frame[] split = NominalFilters.groupByNominal(df, splitIndex);
        double entropy = 0.;
        for (Frame f : split) {
            if (f == null) {
                continue;
            }
            entropy += (1. * f.getRowCount() * entropy(f, classColName)) / (1. * df.getRowCount());
        }
        return entropy;
    }

    public double infoGain(Frame df, String classColName, String splitColName) {
        int splitIndex = df.getColIndex(splitColName);
        Frame[] split = NominalFilters.groupByNominal(df, splitIndex);
        double infoGain = entropy(df, classColName);
        for (Frame f : split) {
            if (f == null) {
                continue;
            }
            infoGain -= (f.getRowCount() / (1. * df.getRowCount())) * entropy(f, classColName);
        }
        return infoGain;
    }
}
