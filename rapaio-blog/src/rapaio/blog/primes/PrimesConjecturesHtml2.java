
package rapaio.blog.primes;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Vector;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.Lines;
import rapaio.io.CsvPersistence;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;

import static rapaio.core.BaseMath.sqrt;
import static rapaio.workspace.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PrimesConjecturesHtml2 {

    public static void main(String[] args) {
        setPrinter(new HTMLPrinter("/home/ati/work/rapaio-blog/src/rapaio/blog/primes/PrimesConjectures.html", "Primes conjectures - part II"));
        new PrimesConjecturesHtml2().run();
    }

    private void run() {
        RandomSource.setSeed(1234);

        Vector primes = loadPrimesVector();
        preparePrinter();
        p("I've always loved to stare at prime numbers. Probably that happens because I "
                + "really do not understand their properties. However, taking into consideration the "
                + "limited capacity of my own skull, I like at least to try to "
                + "understand what others, much more capable than me, have to say about "
                + "prime numbers. ");

        p("And taking about others, I tried these days to find what other "
                + "Romanian fellows have to say about this topic. "
                + "And one interesting thing I found is called Andrica's Conjecture. ");

        heading(4, "Andrica's Conjecture");

        p("Prof. Univ. Dr. Dorin Andrica comes Dr. Andrica works at the Babeş-Bolyai University, Cluj, Romania "
                + "where he is a full professor within the Faculty of Mathematics and Computer Science. ");

        p("This is a list of books from amazon.com which contains his name: "
                + "<a href=\"http://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=dorin%20andrica\">list</a>.");

        p("Among them, there is a book co-authored by the regretted Ioan Cucurezeanu, which was also my own "
                + "Arithmetics professor. My only professor who put me to take the same exam nine times in "
                + "a row (for the whole sumer holiday). ");

        p("Andrica's Conjecture definition can be "
                + "found here: <a href=\"http://planetmath.org/AndricasConjecture\">http://planetmath.org/AndricasConjecture</a>.");

        p("It simply states that: Given the n-th prime \\(p_{n}\\), it is always the case "
                + "that \\(1 > \\sqrt{p_{n+1}}-\\sqrt{p_{n}}\\).");

        p("The conjecture is unproved until now. The description states that the conjecture has been proven "
                + "by computers up to n = 10^5. This looks odd since the vector of primes which I have is learn  "
                + "with a trivial version of a sieve and has " + primes.rowCount() + " prime numbers. ");

        p("As always I try to use my rapaio tools, so plotting a line with the Andrica's gap function is easy like ");

        code("    private void drawAndrica(Vector primes, int size) {\n"
                + "        Frame m = Frames.newMatrixFrame(size, new String[]{\"index\", \"gap\"});\n"
                + "        for (int i = 0; i < m.rowCount(); i++) {\n"
                + "            m.setValue(i, \"index\", i + 1);\n"
                + "            m.setValue(i, \"gap\", sqrt(primes.value(i + 1)) - sqrt(primes.value(i)));\n"
                + "        }\n"
                + "        draw(new Plot()\n"
                + "                .add(new Lines(m.col(\"index\"), m.col(\"gap\")))\n"
                + "                .add(new ABLine(0, true))\n"
                + "                .setYRange(0, 0.7),\n"
                + "                700, 300);\n"
                + "    }\n"
                + "");

        p("First we plot the function for the first 500 values. ");

        drawAndrica(primes, 500, 0.7);

        p("What is already been discovered is that the largest known difference in this function "
                + "happens for a small n, aka for n=4. ");

        p("The plotted function looks like a decreasing function (not monotone, but decreasing on average). "
                + "The question is, if we plot many values the tendency remains the same? ");

        drawAndrica(primes, primes.rowCount() - 1, 0.015);

        p("This are all the prime numbers I have at hand. There are more than 50 millions. And the graph shows "
                + "the tendency is to decrease on average. Which is really interesting. This is not "
                + "a proof, of course, but one could find reasonable to expect the conjecture to be true. Nice work Dr. Andrica!");

        closePrinter();
    }

    private Vector loadPrimesVector() {
        CsvPersistence csv = new CsvPersistence();
        csv.setHasHeader(true);
        csv.getIndexFieldHints().add("primes");
        try {
            Frame df = csv.read("/home/ati/work/rapaio-data/primes/primes.txt");
            return df.col(0);
        } catch (IOException ex) {
        }
        return null;
    }

    private void drawAndrica(Vector primes, int size, double maxyrange) {
        Frame m = Frames.newMatrixFrame(size, new String[]{"index", "gap"});
        for (int i = 0; i < m.rowCount(); i++) {
            m.setValue(i, "index", i + 1);
            m.setValue(i, "gap", sqrt(primes.value(i + 1)) - sqrt(primes.value(i)));
        }
        draw(new Plot()
                .add(new Lines(m.col("index"), m.col("gap")))
                .add(new ABLine(0, true))
                .setYRange(0, maxyrange).setThickerMinSpace(100),
                700, 300);
    }

}
