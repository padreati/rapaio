
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

import static rapaio.workspace.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PrimesConjecturesHtml1 {

    public static void main(String[] args) {
        setPrinter(new HTMLPrinter("/home/ati/work/rapaio-blog/src/rapaio/blog/primes/PrimesConjectures.html", "Primes conjectures - part I"));
        new PrimesConjecturesHtml1().run();
    }

    private void run() {
        RandomSource.setSeed(1234);

        Vector primes = loadPrimesVector();
        preparePrinter();
        p("Very often, my good friend Aurel comes to me with some mathematical facts "
                + "more or less funny, but always interesting. This time he let "
                + "me know about something which is called Chebyshev Bias. ");

        p("Chebyshev noted in 1853, that if you take modulo 4 of the prime numbers, "
                + "the which gives 1 as rest are less than the ones which gives rest 3. "
                + "We denote by \\(\\pi(x,4,1)\\) primes \\(p \\leq x \\), congruent with 1 modulo 4 and "
                + "with \\(\\pi(x,4,3)\\) primes \\(p \\leq x\\), congruent with 3 modulo 4. ");

        p("It is a known fact that \\(\\pi(x,a,b) \\sim \\frac{1}{\\varphi(q)}\\frac{x}{ln(x)}\\). "
                + "That means that this number does not depend on the value of \\(a\\). "
                + "As a consequence, one would expect that \\(\\pi(x,4,1) \\sim \\pi(x,4,3)\\). "
                + "Which is true when \\(x \\to \\infty\\).");

        p("If we draw the ration between \\(\\pi(x,4,3)\\) and \\(\\pi(x,4,1)\\) we can start "
                + "to see what Chebyshev noted. The ratio gets closer to 1, but it seems to be "
                + "almost always a little bit upper than that.");

        drawPoints(primes, 5_000);

        p("But we can get a better picture if we plot the difference between those values, "
                + "not the ratio. We denote by \\(delta(x) = \\pi(x,4,3) - \\pi(x,4,1) \\). "
                + "If we plot delta values for \\( x \\leq 5000\\) we have the plot below. ");
        drawLines(primes, 5_000);

        p("It is obvious that the value of delta function is always greater or equal than 0 with one exception. "
                + "This exception was determined also by Chebyshev, and the value is 2946. "
                + "But this is the only exception? We can see something if we plot delta values for \\(x\\leq 60000\\). ");
        drawLines(primes, 60_000);

        p("It is obvious that there are some points where delta function "
                + "is negative, and those points are around 50380. This time there are many points not just one. ");

        p("If I plot the delta for all the prime number values that I have (in a file computed with a naive sieve), we "
                + "see that the next  time when the delta function has negative values is somewhere around 48 millions, "
                + "which is preety high. ");

        drawLines(primes, primes.rowCount());

        p("It is sure that we can't draw any conclusion from those plots. The delta function looks "
                + "like one which is able to provide unexpected behavior. Howevere it seems safe to "
                + "assume that most of the time the delta function has a positive value. For me, at least, "
                + "this looks like an interesting and unexpected fact.");
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

    private void drawLines(Vector primes, int size) {
        Frame m = Frames.newMatrixFrame(size, "n", "delta");
        int[] count = new int[4];
        for (int i = 0; i < m.rowCount(); i++) {
            count[primes.index(i) % 4]++;

            m.setValue(i, "n", i + 1);
            m.setValue(i, "delta", count[3] - count[1]);
        }
        draw(new Plot()
                .add(new ABLine(0, true).setColorIndex(2))
                .add(new Lines(m.col("n"), m.col("delta")))
                .setThickerMinSpace(80)
                .setBottomLabel("x")
                .setLeftLabel("delta"),
                700, 300
        );
    }

    private void drawPoints(Vector primes, int size) {
        Frame m = Frames.newMatrixFrame(size, "n", "f");
        int[] count = new int[4];
        for (int i = 0; i < m.rowCount(); i++) {
            count[primes.index(i) % 4]++;

            m.setValue(i, "n", i + 1);
            m.setValue(i, "f", (count[1] == 0 ? 0 : count[3] / (1. * count[1])));
        }
        draw(new Plot()
                .add(new ABLine(1, true).setColorIndex(2))
                .add(new Lines(m.col("n"), m.col("f")))
                .setThickerMinSpace(80)
                .setYRange(0.5, 1.5)
                .setBottomLabel("x")
                .setLeftLabel("ratio"),
                700, 300
        );
    }

}
