/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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
 */

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
