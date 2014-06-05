package rapaio.sandbox;

import rapaio.WS;
import rapaio.data.Frame;
import rapaio.datasets.CBenchmark;
import rapaio.datasets.CTask;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.LocalPrinter;
import rapaio.ws.Summary;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class GroovySand {
    public static void main(String[] args) throws IOException {

        WS.setPrinter(new LocalPrinter());
        CTask bench = new CBenchmark().getTask("iris");
        Frame tr = bench.getTrain();
        Summary.summary(tr);

//        draw(new Plot().add(new Histogram(tr.col("sepallength"))))
        WS.draw(new Plot().add(new Points(tr.col("sepallength"), tr.col("petallength")).sz(3).color(tr.col("class")).pch(1)));
    }

}
