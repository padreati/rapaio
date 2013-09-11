/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.explore.Workspace;
import static rapaio.explore.Workspace.*;
import rapaio.printer.HTMLPrinter;
import rapaio.printer.Printer;

import java.io.IOException;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class HistogramTutorial implements TutorialPage {

    @Override
    public String getPageName() {
        return "HistogramTutorial";
    }

    @Override
    public String getPageTitle() {
        return "Histogram Tutorial";
    }

    @Override
    public void render() throws IOException {
        heading(1, "Histogram tutorial");

        p("First we need to load a frame with data. For convenience we don't use " +
                "input/output facilities. Instead, we load a set of data already " +
                "built-in the library.");

        Frame df = Datasets.loadPearsonHeightDataset();
        code("Frame df = Datasets.loadPearsonHeightDataset();");

        p("test");
    }
}
