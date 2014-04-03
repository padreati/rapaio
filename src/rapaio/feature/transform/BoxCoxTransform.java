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

import rapaio.data.Frame;
import rapaio.data.Vector;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class BoxCoxTransform implements Transform {

    private final String[] colNames;
    private final double lambda;

    public BoxCoxTransform(Frame df, String[] colNames, double lambda) {
        this.colNames = colNames;
        this.lambda = lambda;
    }

    @Override
    public void scale(Frame df) {
        for (String colName : colNames) {
            double gm = gm(df.col(colName));
            df.col(colName).stream().transformValue(
                    x -> (lambda == 0) ?
                            gm * Math.log(x) :
                            (Math.pow(x, lambda) - 1.0) / (lambda * Math.pow(gm, lambda - 1))
            );
        }
    }

    @Override
    public void unscale(Frame df) {

    }

    private double gm(Vector v) {
        double p = 1;
        double count = 0;
        for (int i = 0; i < v.rowCount(); i++) {
            if (!v.isMissing(i)) {
                count++;
            }
        }
        for (int i = 0; i < v.rowCount(); i++) {
            if (!v.isMissing(i)) {
                p *= Math.pow(v.getValue(i), 1 / count);
            }
        }
        return p;
    }
}
