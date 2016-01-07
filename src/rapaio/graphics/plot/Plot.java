/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Var;
import rapaio.experiment.grid.MeshGrid;
import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.opt.GOpt;
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

    public Plot(GOpt... opts) {
        bottomThick(true);
        bottomMarkers(true);
        leftThick(true);
        leftMarkers(true);
        this.options.apply(opts);
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
        pc.initialize(this);
        components.add(pc);
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

    public Plot hist(Var v, GOpt... opts) {
        add(new Histogram(v, opts));
        return this;
    }

    public Plot hist(Var v, double minValue, double maxValue, GOpt... opts) {
        add(new Histogram(v, minValue, maxValue, opts));
        return this;
    }

    public Plot hist2d(Var x, Var y, GOpt... opts) {
        add(new Histogram2D(x, y, opts));
        return this;
    }

    public Plot points(Var x, Var y, GOpt... opts) {
        add(new Points(x, y, opts));
        return this;
    }

    public Plot lines(Var y, GOpt... opts) {
        add(new Lines(y, opts));
        return this;
    }

    public Plot lines(Var x, Var y, GOpt... opts) {
        add(new Lines(x, y, opts));
        return this;
    }

    public Plot hLine(double a, GOpt... opts) {
        add(new ABLine(true, a, opts));
        return this;
    }

    public Plot vLine(double a, GOpt... opts) {
        add(new ABLine(false, a, opts));
        return this;
    }

    public Plot abLine(double a, double b, GOpt... opts) {
        add(new ABLine(a, b, opts));
        return this;
    }

    public Plot funLine(SFunction<Double, Double> f, GOpt... opts) {
        add(new FunctionLine(f, opts));
        return this;
    }

    public Plot densityLine(Var var, GOpt... opts) {
        add(new DensityLine(var, opts));
        return this;
    }

    public Plot densityLine(Var var, double bandwidth, GOpt... opts) {
        add(new DensityLine(var, bandwidth, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, GOpt... opts) {
        add(new DensityLine(var, kfunc, opts));
        return this;
    }

    public Plot densityLine(Var var, KFunc kfunc, double bandwidth, GOpt... opts) {
        add(new DensityLine(var, kfunc, bandwidth, opts));
        return this;
    }

    public Plot rocCurve(ROC roc, GOpt... opts) {
        add(new ROCCurve(roc, opts));
        return this;
    }

    public Plot meshContour(MeshGrid mg, boolean contour, boolean fill, GOpt... opts) {
        add(new MeshContour(mg, contour, fill, opts));
        return this;
    }

    public Plot legend(double x, double y, GOpt... opts) {
        add(new Legend(x, y, opts));
        return this;
    }

    public Plot segment2d(double x1, double y1, double x2, double y2, GOpt... opts) {
        add(new Segment2D(x1, y1, x2, y2, opts));
        return this;
    }
}
