package sample;

import rapaio.data.IndexOneVector;
import rapaio.distributions.Normal;
import rapaio.distributions.StudentT;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.FunctionLine;
import rapaio.printer.RemotePrinter;

import static rapaio.explore.Workspace.draw;
import static rapaio.explore.Workspace.setPrinter;

/**
 * @author Aurelian Tutuianu
 */
public class NormalDistribution {

    public static void main(String[] args) {

        setPrinter(new RemotePrinter());

        Plot p = new Plot();

        FunctionLine fline = new FunctionLine(p, new StudentT(3).getPdfFunction());
        p.add(fline);
        p.getOp().setXRange(-4, 4);
        p.getOp().setYRange(0, 0.5);

        FunctionLine normalpdf = new FunctionLine(p, new Normal().getPdfFunction());
        normalpdf.opt().setColorIndex(new IndexOneVector(1));
        p.add(normalpdf);

        draw(p);
    }
}
