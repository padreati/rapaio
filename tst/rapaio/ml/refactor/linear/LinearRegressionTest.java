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

package rapaio.ml.refactor.linear;

import org.junit.Test;
import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.Points;
import rapaio.printer.LocalPrinter;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static rapaio.data.Vectors.newNumFrom;
import static rapaio.workspace.W.draw;
import static rapaio.workspace.W.setPrinter;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class LinearRegressionTest {

    //    @Test
    public void simpleLinearTest() {

        List<Vector> vectors = new ArrayList<>();
        vectors.add(newNumFrom(1, 1, 1, 1, 1));
        vectors.add(newNumFrom(1, 2, 3, 4, 5));
        vectors.add(newNumFrom(1, 4, 9, 16, 25));
        vectors.add(newNumFrom(2.8, 3.2, 7.1, 6.8, 8.8));

        List<String> names = new ArrayList<>();
        names.add("x0");
        names.add("x1");
        names.add("x2");
        names.add("y1");

        Frame df = new SolidFrame(5, vectors, names);

        LinearModelRegressor lm = new LinearModelRegressor();
        lm.learn(df, "y1");

        Summary.lines(lm.getCoeff());
        Summary.lines(lm.getFitValues());

    }

    //    @Test
    public void pearsonTest() throws IOException, URISyntaxException {

        setPrinter(new LocalPrinter());
        Frame df = Datasets.loadPearsonHeightDataset();

        Vector intercept = Vectors.newNum(df.rowCount(), 1);
        df = Frames.addCol(df, intercept, "I", 0);
        Summary.summary(df);

        LinearModelRegressor lm = new LinearModelRegressor();
        lm.learn(df, "Son");

        Summary.lines(lm.getCoeff());

        draw(new Plot()
                        .add(new Points(df.col("Father"), df.col("Son"))
                                        .setPch(1).setSize(1)
                        )
                        .add(new ABLine(
                                lm.getCoeff().getValue(1, 1),
                                lm.getCoeff().getValue(0, 1)))
                        .setXLim(58, 78)
                        .setYLim(58, 78)
                        .setXLab("Father")
                        .setYLab("Son")
        );
    }

    @Test
    public void testESTL() throws IOException {
        setPrinter(new LocalPrinter());
        Frame df = Datasets.loadProstateCancer();

        Numeric inter = Vectors.newNum(df.rowCount(), 1.);
//		df = Frames.addCol(df, inter, "inter", 0);

        Frame bf = new SolidFrame(
                df.rowCount(),
                new Vector[]{df.col(0)},
                new String[]{df.colNames()[0]});

        for (int i = 1; i < df.colCount(); i++) {
            bf = Frames.addCol(bf, df.col(i), df.colNames()[i], 0);
        }

        LinearModelRegressor lm1 = new LinearModelRegressor();
        lm1.learn(df, "lpsa");
        LinearModelRegressor lm2 = new LinearModelRegressor();
        lm2.learn(bf, "lpsa");

        Summary.lines(lm1.getCoeff());
        Summary.lines(lm2.getCoeff());
    }
}
