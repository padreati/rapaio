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

package rapaio.experiment.grid;

import rapaio.core.CoreTools;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/15.
 */
public class MeshGridFactory {

    public static MeshGrid1D buildFrom(Classifier c, Frame df, String x1Name, String x2Name, int steps, String labelName) {

        double x1min = CoreTools.min(df.var(x1Name)).value();
        double x1max = CoreTools.max(df.var(x1Name)).value();
        double x2min = CoreTools.min(df.var(x2Name)).value();
        double x2max = CoreTools.max(df.var(x2Name)).value();

        Numeric x1 = Numeric.newSeq(x1min, x1max, (x1max - x1min) / steps).withName(x1Name);
        Numeric x2 = Numeric.newSeq(x2min, x2max, (x2max - x2min) / steps).withName(x2Name);

        MeshGrid1D mg = new MeshGrid1D(x1, x2);

        Numeric f1 = Numeric.newEmpty().withName(x1Name);
        Numeric f2 = Numeric.newEmpty().withName(x2Name);

        for (int i = 0; i < x1.rowCount(); i++) {
            for (int j = 0; j < x2.rowCount(); j++) {
                f1.addValue(x1.value(i));
                f2.addValue(x2.value(j));
            }
        }
        CFit fit = c.fit(SolidFrame.newWrapOf(f1, f2));
        int pos = 0;
        for (int i = 0; i < x1.rowCount(); i++) {
            for (int j = 0; j < x2.rowCount(); j++) {
                mg.setValue(i, j, fit.firstDensity().value(pos++, labelName));
            }
        }

        return mg;
    }
}
