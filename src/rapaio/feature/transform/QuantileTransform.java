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

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class QuantileTransform implements Transform {

    private final String[] colNames;
    private final double[] p;
    private final Map<String, double[]> values;
    private final double width;

    public QuantileTransform(Var v, String name, int n) {
        this.width = 1.0 / (1.0 * n);
        this.colNames = new String[]{name};
        this.p = new double[n + 1];
        for (int i = 0; i < n; i++) {
            p[i + 1] = (i + 1.0) / (1.0 * n);
        }
        values = new HashMap<>();
        values.put(name, new Quantiles(v, p).getValues());
    }

    public QuantileTransform(Frame df, String[] colNames, int n) {
        this.width = 1.0 / (1.0 * n);
        this.colNames = colNames;
        p = new double[n + 1];
        for (int i = 0; i < n; i++) {
            p[i + 1] = (i + 1.0) / (1.0 * n);
        }
        values = new HashMap<>();
        for (String colName : colNames) {
            values.put(colName, new Quantiles(df.col(colName), p).getValues());
        }
    }

    public void scale(Frame df) {
        for (String colName : colNames) {
            Var col = df.col(colName);
            double[] vals = values.get(colName);
            for (int i = 0; i < df.rowCount(); i++) {
                if (col.missing(i)) continue;
                double value = col.value(i);
                if (value <= vals[0]) {
                    col.setValue(i, 0.0);
                    continue;
                }
                if (value >= vals[vals.length - 1]) {
                    col.setValue(i, 1.0);
                    continue;
                }
                int next = Arrays.binarySearch(vals, value);
                if (next < 0) next = -next - 1;
                next--;
                col.setValue(i, p[next] + (vals[next] != vals[next + 1] ?
                        width * (value - vals[next]) / (vals[next + 1] - vals[next])
                        : 0));
            }
        }
    }

    public void unscale(Frame df) {
        for (String colName : colNames) {
            Var col = df.col(colName);
            double[] vals = values.get(colName);
            for (int i = 0; i < df.rowCount(); i++) {
                if (col.missing(i)) continue;
                double value = col.value(i);
                if (value <= 0) {
                    col.setValue(i, vals[0]);
                    continue;
                }
                if (value >= 1.0) {
                    col.setValue(i, vals[vals.length - 1]);
                    continue;
                }
                int next = Arrays.binarySearch(p, value);
                if (next < 0) next = -next - 1;
                next--;
                col.setValue(i, vals[next] + (vals[next + 1] - vals[next]) * (value - p[next]));
            }
        }
    }


    public Var scale(Var v, String name) {
        double[] vals = values.get(name);
        for (int i = 0; i < v.rowCount(); i++) {
            if (v.missing(i)) continue;
            double value = v.value(i);
            if (value <= vals[0]) {
                v.setValue(i, 0.0);
                continue;
            }
            if (value >= vals[vals.length - 1]) {
                v.setValue(i, 1.0);
                continue;
            }
            int next = Arrays.binarySearch(vals, value);
            if (next < 0) next = -next - 1;
            next--;
            v.setValue(i, p[next] + (vals[next] != vals[next + 1] ?
                    width * (value - vals[next]) / (vals[next + 1] - vals[next])
                    : 0));
        }
        return v;
    }

}
