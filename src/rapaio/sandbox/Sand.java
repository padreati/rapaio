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
public class Sand {
    public static void main(String[] args) throws IOException {

        WS.setPrinter(new LocalPrinter());
        CTask bench = new CBenchmark().getTask("iris");
        Frame tr = bench.getTrainFrame();
        Summary.summary(tr);

//        draw(new Plot().add(new Histogram(tr.var("sepallength"))))
        WS.draw(new Plot().add(new Points(tr.var("sepallength"), tr.var("petallength")).sz(3).color(tr.var("class")).pch(1)));
    }

}
