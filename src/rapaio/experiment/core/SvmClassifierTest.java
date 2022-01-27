/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.core;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.PCH_CIRCLE_FULL;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.pch;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.svm.SVMClassifier;
import rapaio.sys.Experimental;
import rapaio.sys.WS;

@Experimental
public class SvmClassifierTest {

    public static void main(String[] args) {

        double gamma = 50;
        double c = 1;

        SVMClassifier svm1 = new SVMClassifier()
                .kernel.set(new RBFKernel(gamma))
                .c.set(c)
                .probability.set(true);

        SVMClassifier svm2 = new SVMClassifier()
                .kernel.set(new RBFKernel(gamma))
                .c.set(c)
                .probability.set(false);

        Frame iris = Datasets.loadIrisDataset().mapVars(VarRange.of(0, 1, 4)).copy();

        svm1.fit(iris, "class");
        svm2.fit(iris, "class");

        var x1 = iris.rvar(0);
        var x2 = iris.rvar(1);


        GridLayer grid = new GridLayer(1, 2);

        Plot plot1 = plot();
        VectorDataGrid mg1 = computMesh(svm1, x1, x2);
        mg1.plot(plot1, 0, 1, 20);
        plot1.points(iris.rvar(0), iris.rvar(1), pch(PCH_CIRCLE_FULL), fill(iris.rvar(2)));
        grid.add(plot1);

        Plot plot2 = plot();
        VectorDataGrid mg2 = computMesh(svm2, x1, x2);
        mg2.plot(plot2, Double.NaN, Double.NaN, 20);
        plot2.points(iris.rvar(0), iris.rvar(1), pch(PCH_CIRCLE_FULL), fill(iris.rvar(2)));
        grid.add(plot2);


        WS.draw(grid);
    }

    private static VectorDataGrid computMesh(SVMClassifier svm1, Var x1, Var x2) {
        VectorDataGrid mg = new VectorDataGrid(3, x1.dv().min(), x1.dv().max(), x2.dv().min(), x2.dv().max(), 0.005);
        var x = mg.getXRange();
        var y = mg.getYRange();

        Frame dfmesh = SolidFrame.byVars(
                VarDouble.empty(x.size() * y.size()).name(x1.name()),
                VarDouble.empty(x.size() * y.size()).name(x2.name())
        );
        int pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                dfmesh.setDouble(pos, 0, x.getDouble(i));
                dfmesh.setDouble(pos, 1, y.getDouble(j));
                pos++;
            }
        }

        Frame testMesh = svm1.predict(dfmesh).firstDensity();
        pos = 0;
        for (int i = 0; i < x.size(); i++) {
            for (int j = 0; j < y.size(); j++) {
                DVector pred = DVector.wrap(testMesh.getDouble(pos, 1), testMesh.getDouble(pos, 2), testMesh.getDouble(pos, 3));
                mg.set(i, j, pred);
                pos++;
            }
        }
        return mg;
    }
}
