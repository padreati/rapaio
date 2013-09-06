package rapaio.graphics.plot;

import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.options.GraphicOptions;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public abstract class PlotComponent {

    private final GraphicOptions options;
    protected final Plot plot;

    public PlotComponent(Plot plot) {
        this.plot = plot;
        this.options = new GraphicOptions(plot.getOp());
    }

    public GraphicOptions opt() {
        return options;
    }

    public abstract Range getComponentDataRange();

    public abstract void paint(Graphics2D g2d);

    public double xscale(double x) {
        return plot.xscale(x);
    }

    public double yscale(double y) {
        return plot.yscale(y);
    }
}
