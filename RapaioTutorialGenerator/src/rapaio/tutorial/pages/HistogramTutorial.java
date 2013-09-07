package rapaio.tutorial.pages;

import static rapaio.explore.Workspace.*;
import rapaio.printer.HTMLPrinter;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class HistogramTutorial {

    public static void main(String[] args) {

        setPrinter(new HTMLPrinter("_pages/HistogramTutorial.html", "Histogram Tutorial"));

        preparePrinter();

        p("test");

        closePrinter();
    }
}
