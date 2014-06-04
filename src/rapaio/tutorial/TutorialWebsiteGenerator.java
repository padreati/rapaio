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

package rapaio.tutorial;

import rapaio.printer.HTMLPrinter;
import rapaio.tutorial.pages.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static rapaio.WS.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class TutorialWebsiteGenerator {

    private static final Logger logger = Logger.getLogger("rapaio");

    private static final String TUTORIAL_WEB_ROOT = "/home/ati/rapaio/rapaio-tutorial/";

    public static void main(String[] args) throws IOException, URISyntaxException {

        logger.setLevel(Level.FINEST);
        logger.addHandler(new ConsoleHandler() {
            {
                setFormatter(new SimpleFormatter());
            }
        });

        File webRoot = new File(TUTORIAL_WEB_ROOT);
        File pageRoot = new File(webRoot, "pages");

        deleteRoot(pageRoot);
        deleteRoot(new File(webRoot, "index.html"));

        webRoot.mkdir();
        pageRoot.mkdir();

        TreeMap<String, List<TutorialPage>> pages = new TreeMap<>();

        pages.put("Graphics", Arrays.asList(
//                new HistogramDensityTutorial()
                ));

        pages.put("StatisticalProcedures", Arrays.asList(
//                new DiscreteSamplingPage(),
//                new CorrelationsPage(),
//                new ROCCurvesPage()
        ));

        pages.put("SampleAnalysis", Arrays.asList(
//                new PearsonHeight(),
//                new LawOfLargeNumbers()
        ));

        pages.put("SupervisedClassification", Arrays.asList(
//                new ClassificationWithRF()
        ));

        pages.put("Regression", Arrays.asList(
                new LinearModel1Page()//,
//                new LinearRegression2Page(),
//                new LinearRegression3Page()
        ));

        pages.put("WorkInProgress", Arrays.asList(
//                new IrisExplore(),
//                new StudentTDistribution()
        ));

        makeIndexPage(webRoot, pages);

        for (String categ : pages.keySet()) {
            File categoryRoot = new File(pageRoot, categ);
            if (!categoryRoot.exists()) {
                categoryRoot.mkdir();
            }
            for (TutorialPage page : pages.get(categ)) {
                File pageFile = new File(categoryRoot, page.getPageName() + ".html");
                setPrinter(new HTMLPrinter(pageFile.getAbsolutePath(), page.getPageTitle(), "<a href=\"../../index.html\">Back</a>"));
                preparePrinter();
                page.render();
                closePrinter();
            }
        }
    }

    private static void makeIndexPage(File webRoot, TreeMap<String, List<TutorialPage>> pages) {
        File indexPage = new File(webRoot, "index.html");
        setPrinter(new HTMLPrinter(indexPage.getAbsolutePath(), "Rapaio Tutorials"));
        preparePrinter();

        heading(1, "Rapaio Tutorial Pages");

        p("This is the home page for Rapaio tutorials.");

        p("A rapaio tutorial page is a document generated with rapaio "
                + "printer in order to exemplify how "
                + "an analysis could be accomplished using Rapaio statistical,"
                + "data mining and machine learning toolbox.");

        p("Most of the tutorials will be strictly oriented on a small "
                + "small rapaio facility. As a sample how one ca read "
                + "and write data with CVSPersistence. Other tutorials will be oriented "
                + "on the pieces of output facilities useful in data visualization, "
                + "either as text or as graphical images. ");

        p("There are also some tutorials which tries to do an exploration "
                + "analysis, trying to put together the small pieces into something "
                + "fluent and understandable as a whole. However, So, these pages does not "
                + "contain a full exploration analysis or other getType of analysis. "
                + "Its purpose is to illustrate how Rapaio toolbox could be used. "
                + "They are not a golden-standard for how an exploration must be conduct.");

        p("From time to time the whole tutorials will be regenerated and republished.");

        p("That happens because they will grow together with the library and its "
                + "facilities. The tutorials are generated from source code, using Rapaio "
                + "library documenting facilities. The advantage of writing tutorials in this "
                + "manner is that they will remain comaptible with the last revision of "
                + "the library, thus they will be up-to-date and ready for immediate usage.");

        p("Have fun on learning and using Rapaio.");

        heading(2, "Rapaio Tutorial Gallery");

        pages.keySet().stream().map((category) -> {
            heading(3, category);
            return category;
        }).forEach((category) -> {
            pages.get(category).stream().forEach((page) -> {
                print("<a href=\"pages/" + category + "/" + page.getPageName() + ".html\">" + page.getPageTitle() + "</a></br>");
            });
        });

        closePrinter();
    }

    private static void deleteRoot(File root) {
        if (root.isDirectory()) {
            for (File child : root.listFiles()) {
                deleteRoot(child);
            }
        }
        root.delete();
    }
}
