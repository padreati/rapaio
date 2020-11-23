/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/15.
 */
public class MeshGridFactory {

    public static MeshGrid1D buildFrom(ClassifierModel c, Frame df, String x1Name, String x2Name, int steps, String labelName) {

        double x1min = Minimum.of(df.rvar(x1Name)).value();
        double x1max = Maximum.of(df.rvar(x1Name)).value();
        double x2min = Minimum.of(df.rvar(x2Name)).value();
        double x2max = Maximum.of(df.rvar(x2Name)).value();

        VarDouble x1 = VarDouble.seq(x1min, x1max, (x1max - x1min) / steps).name(x1Name);
        VarDouble x2 = VarDouble.seq(x2min, x2max, (x2max - x2min) / steps).name(x2Name);

        MeshGrid1D mg = new MeshGrid1D(x1, x2);

        VarDouble f1 = VarDouble.empty().name(x1Name);
        VarDouble f2 = VarDouble.empty().name(x2Name);

        for (int i = 0; i < x1.rowCount(); i++) {
            for (int j = 0; j < x2.rowCount(); j++) {
                f1.addDouble(x1.getDouble(i));
                f2.addDouble(x2.getDouble(j));
            }
        }
        ClassifierResult fit = c.predict(SolidFrame.byVars(f1, f2));
        int pos = 0;
        for (int i = 0; i < x1.rowCount(); i++) {
            for (int j = 0; j < x2.rowCount(); j++) {
                mg.setValue(i, j, fit.firstDensity().getDouble(pos++, labelName));
            }
        }

        return mg;
    }
}
