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

package rapaio.feature.transform;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class StandardTransform implements Transform {

    private final String[] colNames;
    private final Map<String, Double> mean;
    private final Map<String, Double> sd;

    public StandardTransform(Frame df, String[] colNames) {
        this.colNames = colNames;
        this.mean = new HashMap<>();
        this.sd = new HashMap<>();

        for (String colName : colNames) {
            Var col = df.col(colName);
            mean.put(colName, new Mean(col).getValue());
            sd.put(colName, Math.sqrt(new Variance(col).getValue()));
        }
    }

    @Override
    public void scale(Frame df) {
        for (String colName : colNames) {
            df.col(colName).stream().transformValue(
                    x -> (x - mean.get(colName)) / sd.get(colName));
        }
    }

    @Override
    public void unscale(Frame df) {
        for (String colName : colNames) {
            df.col(colName).stream().transformValue(
                    x -> x * sd.get(colName) + mean.get(colName)
            );
        }
    }
}
