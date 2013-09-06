package rapaio.graphics.plot;

import rapaio.data.Vector;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;
import rapaio.graphics.pch.PchPalette;

import java.awt.*;

/**
 * @author tutuianu
 */
public class Points extends PlotComponent {

    private final Vector x;
    private final Vector y;

    public Points(Plot parent, Vector x, Vector y) {
        super(parent);
        this.x = x;
        this.y = y;

        if (plot.getBottomLabel() == null) {
            plot.setBottomLabel(x.getName());
        }
        if (plot.getLeftLabel() == null) {
            plot.setLeftLabel(y.getName());
        }
    }

    @Override
    public Range getComponentDataRange() {
        if (x.getRowCount() == 0) {
            return null;
        }
        Range range = new Range();
        for (int i = 0; i < x.getRowCount(); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            range.union(x.getValue(i), y.getValue(i));
        }
        return range;
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 0; i < x.getRowCount(); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            g2d.setColor(opt().getColor(i));
            int xx = (int) (plot.xscale(x.getValue(i)));
            int yy = (int) (plot.yscale(y.getValue(i)));
            PchPalette.STANDARD.draw(g2d, xx, yy, opt().getSize(i), opt().getPch(i));
        }
    }
}
