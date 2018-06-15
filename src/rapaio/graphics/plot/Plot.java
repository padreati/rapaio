/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.graphics.plot;

import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.IdxVar;
import rapaio.data.Var;
import rapaio.experiment.grid.MeshGrid;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.plotcomp.*;
import rapaio.ml.eval.ROC;
import rapaio.util.func.SFunction;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
public class Plot extends HostFigure {

    private static final long serialVersionUID = 1898871481989584539L;
    private final List<PlotComponent> components = new LinkedList<>();

    public Plot(GOption... opts) {
        bottomThick(true);
        bottomMarkers(true);
        leftThick(true);
        leftMarkers(true);
        this.options.bind(opts);
    }

    @Override
    protected Range buildRange() {
        Range range = null;
        for (PlotComponent pc : components) {
            Range pcRange = pc.getRange();
            if (pcRange != null)
                if (range == null) range = pcRange;
                else range.union(pcRange);
        }

        if (range == null) {
            range = new Range(0, 0, 1, 1);
        } else {
            range = range.getExtendedRange();
        }

        if (x1 == x1 && x2 == x2) {
            range.setX1(x1);
            range.setX2(x2);
        }
        if (y1 == y1 && y2 == y2) {
            range.setY1(y1);
            range.setY2(y2);
        }

        if (range.y1() == range.y2()) {
            range.setY1(range.y1() - 0.5);
            range.setY2(range.y2() + 0.5);
        }
        return range;
    }

    public List<PlotComponent> getComponents() {
        return components;
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        buildRange();
        super.paint(g2d, rect);
        for (PlotComponent pc : components) {
            pc.paint(g2d);
        }
    }

    @Override
    protected void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    protected void buildBottomMarkers() {
        buildNumericBottomMarkers();
    }

    public Plot add(PlotComponent pc) {
        pc.bind(this);
        components.add(pc);
        return this;
    }

    @Override
    public Plot title(String title) {
        super.title(title);
        return this;
    }

    @Override
    public Plot bottomMarkers(boolean bottomMarkers) {
        super.bottomMarkers(bottomMarkers);
        return this;
    }

    @Override
    public Plot leftMarkers(boolean leftMarkers) {
        super.leftMarkers(leftMarkers);
        return this;
    }

    @Override
    public Plot xLab(String xLab) {
        super.xLab(xLab);
        return this;
    }

    @Override
    public Plot yLab(String yLab) {
        super.yLab(yLab);
        return this;
    }

    // OPTIONS

    public Plot xLim(double start, double end) {
        return (Plot) super.xLim(start, end);
    }

    public Plot yLim(double start, double end) {
        return (Plot) super.yLim(start, end);
    }


    // COMPONENTS

    public Plot hist(Var v, GOption... opts) {
        add(new Histogram(v, opts));
        return this;
    }

    public Plot hist(Var v, double minValue, double maxValue, GOption... opts) {
        add(new Histogram(v, minValue, maxValue, opts));
        return this;
    }

    public Plot hist2d(Var x, Var y, GOption... opts) {
        add(new Histogram2D(x, y, opts));
        return this;
    }

    public Plot points(Var x, Var y, GOption... opts) {
        add(new Points(x, y, opts));
        return this;
    }

    public Plot lines(Var y, GOption... opts) {
        add(new Lines(y, opts));
        return this;
    }

    public Plot lines(Var x, Var y, GOption... opts) {
        add(new Lines(x, y, opts));
        return this;
    }

    public Plot hLine(double a, GOption... opts) {
        add(new ABLine(true, a, opts));
        return this;
    }

    public Plot vLine(double a, GOption... opts) {
        add(new ABLine(false, a, opts));
        return this;
    }

    public Plot abLine(double a, double b, GOption... opts) {
        add(new ABLine(a, b, opts));
        return this;
    }

    public Plot funLine(SFunction<Double, Double> f, GOption... opts) {
        add(new FunctionLine(f, opts));
        return this;
    }

    public Plot densityLine(Var var, GOption... opts) {
        add(new DensityLine(var, opts));
        return this;
    }

    public Plot densityLine(Var var, double bandwidth, GOption... opts) {
        add(new DensityLine(var, bandwidth, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, GOption... opts) {
        add(new DensityLine(var, kfunc, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOption... opts) {
        add(new DensityLine(var, kfunc, bandwidth, opts));
        return this;
    }

    public Plot rocCurve(ROC roc, GOption... opts) {
        add(new ROCCurve(roc, opts));
        return this;
    }

    public Plot meshContour(MeshGrid mg, boolean contour, boolean fill, GOption... opts) {
        add(new MeshContour(mg, contour, fill, opts));
        return this;
    }

    public Plot legend(double x, double y, GOption... opts) {
        add(new Legend(x, y, opts));
        return this;
    }

    public Plot legend(int place, GOption... opts) {
        add(new Legend(place, opts));
        return this;
    }

    public Plot segment2d(double x1, double y1, double x2, double y2, GOption... opts) {
        add(new Segment2D(x1, y1, x2, y2, opts));
        return this;
    }

    public Plot dvLines(Var values) {
        add(new DVLines(values, IdxVar.seq(values.rowCount())));
        return this;
    }

    public Plot dvLines(Var values, Var indexes, GOption... opts) {
        add(new DVLines(values, indexes, opts));
        return this;
    }
}
