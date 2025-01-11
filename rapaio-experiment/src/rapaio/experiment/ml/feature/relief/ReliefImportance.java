/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.ml.feature.relief;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.util.collection.Ints;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/19/18.
 */
public record ReliefImportance(String[] names, double[] weights) {

    public Frame orderedFrame() {
        int[] rows = Ints.seq(0, names.length);
        Ints.quickSort(rows, 0, names.length, (r1, r2) -> -Double.compare(weights[r1], weights[r2]));

        VarDouble weightVar = VarDouble.empty().name("weights");
        VarNominal nameVar = VarNominal.empty().name("names");

        for (int row : rows) {
            nameVar.addLabel(names[row]);
            weightVar.addDouble(weights[row]);
        }

        return SolidFrame.byVars(nameVar, weightVar);
    }

    public Frame unorderedFrame() {
        VarDouble weightVar = VarDouble.empty().name("weights");
        VarNominal nameVar = VarNominal.empty().name("names");

        for (int i = 0; i < names.length; i++) {
            nameVar.addLabel(names[i]);
            weightVar.addDouble(weights[i]);
        }

        return SolidFrame.byVars(nameVar, weightVar);
    }
}
