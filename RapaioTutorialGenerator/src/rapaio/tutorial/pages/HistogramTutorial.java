package rapaio.tutorial.pages;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import static rapaio.explore.Workspace.*;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class HistogramTutorial {

    public static void main(String[] args) throws IOException {

        setPrinter(new HTMLPrinter("pages/HistogramTutorial.html", "Histogram Tutorial"));

        preparePrinter();

        heading(1, "Histogram tutorial");

        p("First we need to load a frame with data. For convenience we don't use " +
                "input/output facilities. Instead, we load a set of data already " +
                "built-in the library.");

        Frame df = Datasets.loadPearsonHeightDataset();
        code("Frame df = Datasets.loadPearsonHeightDataset();");


        p("test");

        closePrinter();
    }
}
