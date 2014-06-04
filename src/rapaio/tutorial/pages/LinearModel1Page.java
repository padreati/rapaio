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
package rapaio.tutorial.pages;

import java.io.IOException;
import java.net.URISyntaxException;
import static rapaio.WS.*;
import rapaio.data.Frame;
import static rapaio.data.MathNumeric.*;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.ws.Summary;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class LinearModel1Page implements TutorialPage {

    @Override
    public String getPageName() {
        return "Linear Regression 1";
    }

    @Override
    public String getPageTitle() {
        return "Linear Regression: predict with a constant";
    }

    @Override
    public void render() throws IOException, URISyntaxException {

        heading(3, "Linear Regression with vectors and matrices - part 1");

        p("This tutorial aims to present how one can do by hand linear regression "
                + "using only vectors and matrices operations. For practical "
                + "purposes it should be used linear regression models. ");

        heading(4, "Predict with a constant");

        p("We will use the well-known data set Father-Son Heights. "
                + "This data was collected by Karl Pearson and contains the "
                + "heights of the fathers and their full-grown sons, "
                + "in England, circa 1900. ");

        p("This data set is used to study the relation between the fathers and "
                + "the sons heights. However for the purpose of this tutorial "
                + "we are interested only in the sons heights. ");

        Frame df = Datasets.loadPearsonHeightDataset();
        Summary.summary(df);

        heading(4, "Question: Given collected sons heights, can we find a constant which "
                + "can be used for prediction? ");

        p("First we get the heights of the sons in a numeric var. ");

        Numeric sons = (Numeric) df.col("Son");

        code(""
                + "        Frame df = Datasets.loadPearsonHeightDataset();\n"
                + "        Summary.summary(df);\n"
                + "");

        p("We ");

        code("mean: " + mean(sons).value(0));
        code("variance: " + var(sons).value(0));
        code("sd: " + sd(sons).value(0));
    }
}
