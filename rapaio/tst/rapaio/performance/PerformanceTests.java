package rapaio.performance;

import org.junit.Test;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NumVector;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
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

        final int TESTS = 500;
        final int LEN = 100_000;

        NumVector index = new NumVector();
        NumVector time1 = new NumVector();
        NumVector time2 = new NumVector();
        NumVector delta = new NumVector();

        for (int i = 0; i < TESTS; i++) {

            long start = System.currentTimeMillis();
            double[] values = new double[LEN];
            for (int j = 0; j < LEN; j++) {
                values[j] += j * Math.sin(j);
            }
            time1.add(System.currentTimeMillis() - start);

            start = System.currentTimeMillis();
            NumVector numVector = new NumVector(LEN);
            for (int j = 0; j < LEN; j++) {
                numVector.add(j * Math.sin(j));
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
                .add(new Lines(index, time1).setColorIndex(1))
                .add(new Lines(index, time2).setColorIndex(2))
                .setBottomLabel("array")
                .setLeftLabel("NumVector")
        );

//        draw(new Plot()
//                .add(new Histogram(delta).setBins(30))
//        );

        new Mean(delta).summary();
        new Variance(delta).summary();
    }
}
