package rapaio.datax;

import org.junit.Test;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NumericVector;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Histogram;
import rapaio.printer.LocalPrinter;

import static rapaio.workspace.Workspace.draw;
import static rapaio.workspace.Workspace.setPrinter;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class PerformanceTests {

    @Test
    public void testNumericVector() {

        setPrinter(new LocalPrinter());

        final int TESTS = 1_000;
        final int LEN = 100_000;

        NumericVector index = new NumericVector();
        NumericVector time1 = new NumericVector();
        NumericVector time2 = new NumericVector();
        NumericVector delta = new NumericVector();

        for (int i = 0; i < TESTS; i++) {

            long start = System.currentTimeMillis();
            double[] values = new double[LEN];
            for (int j = 0; j < LEN; j++) {
                values[j] += j * Math.sin(j);
            }
            time1.add(System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            NumericVector list = new NumericVector();
            for (int j = 0; j < LEN; j++) {
                list.add(j * Math.sin(j));
            }
            time2.add(System.currentTimeMillis() - start);
            index.add(i);
            delta.add(time1.getValue(i) - time2.getValue(i));
        }

//        draw(new Plot()
//                .add(new Lines(index, time1).setColorIndex(1))
//                .add(new Lines(index, time2).setColorIndex(2))
//                .add(new Lines(index, delta).setColorIndex(3))
//        );

        draw(new Plot()
                .add(new Histogram(delta).setBins(30))
        );

        new Mean(delta).summary();
        new Variance(delta).summary();
    }
}
