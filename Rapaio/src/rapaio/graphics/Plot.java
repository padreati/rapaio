package rapaio.graphics;

import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.util.LinkedList;

/**
 * @author tutuianu
 */
public class Plot extends BaseFigure {

    private LinkedList<PlotComponent> components = new LinkedList<>();

    public Plot() {
        bottomThicker = true;
        bottomMarkers = true;
        leftThicker = true;
        leftMarkers = true;
    }

    @Override
    public Range buildRange() {
        Range r = null;
        for (PlotComponent pc : components) {
            Range newrange = pc.getComponentDataRange();
            if (newrange != null) {
                if (r == null) {
                    r = newrange;
                } else {
                    r.union(newrange);
                }
            }
        }

        if (r == null) {
            return null;
        }

        if (getOp().getXRangeStart() == getOp().getXRangeStart() && getOp().getXRangeEnd() == getOp().getXRangeEnd()) {
            r.setX1(getOp().getXRangeStart());
            r.setX2(getOp().getXRangeEnd());
        }
        if (getOp().getYRangeStart() == getOp().getYRangeStart() && getOp().getYRangeEnd() == getOp().getYRangeEnd()) {
            r.setY1(getOp().getYRangeStart());
            r.setY2(getOp().getYRangeEnd());
        }
        return r;
    }

    public void add(PlotComponent component) {
        this.components.add(component);
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);
        for (PlotComponent pc : components) {
            pc.paint(g2d);
        }
    }

    @Override
    public void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    public void buildBottomMarkers() {
        buildNumericBottomMarkers();
    }
}
