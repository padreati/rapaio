/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.NumericVar;
import rapaio.data.SolidFrame;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/15.
 */
public class MeshGridFactory {

    public static MeshGrid1D buildFrom(Classifier c, Frame df, String x1Name, String x2Name, int steps, String labelName) {

        double x1min = CoreTools.min(df.getVar(x1Name)).getValue();
        double x1max = CoreTools.max(df.getVar(x1Name)).getValue();
        double x2min = CoreTools.min(df.getVar(x2Name)).getValue();
        double x2max = CoreTools.max(df.getVar(x2Name)).getValue();

        NumericVar x1 = NumericVar.seq(x1min, x1max, (x1max - x1min) / steps).withName(x1Name);
        NumericVar x2 = NumericVar.seq(x2min, x2max, (x2max - x2min) / steps).withName(x2Name);

        MeshGrid1D mg = new MeshGrid1D(x1, x2);

        NumericVar f1 = NumericVar.empty().withName(x1Name);
        NumericVar f2 = NumericVar.empty().withName(x2Name);

        for (int i = 0; i < x1.getRowCount(); i++) {
            for (int j = 0; j < x2.getRowCount(); j++) {
                f1.addValue(x1.getValue(i));
                f2.addValue(x2.getValue(j));
            }
        }
        CFit fit = c.fit(SolidFrame.byVars(f1, f2));
        int pos = 0;
        for (int i = 0; i < x1.getRowCount(); i++) {
            for (int j = 0; j < x2.getRowCount(); j++) {
                mg.setValue(i, j, fit.firstDensity().getValue(pos++, labelName));
            }
        }

        return mg;
    }
}
