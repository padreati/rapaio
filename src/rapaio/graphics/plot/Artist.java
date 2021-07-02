/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot;

import rapaio.graphics.opt.GOptions;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author Aurelian Tutuianu
 */
public abstract class Artist implements Serializable {

    @Serial
    private static final long serialVersionUID = -797168275849511614L;
    protected final GOptions options = new GOptions();
    protected Plot plot;

    public void bind(Plot plot) {
        this.plot = plot;
        this.options.setParent(plot.options);
        if (this.plot.xAxis().type().equals(Axis.Type.UNKNOWN)) {
            this.plot.xAxis().type(xAxisType());
        }
        if (this.plot.yAxis().type().equals(Axis.Type.UNKNOWN)) {
            this.plot.yAxis().type(yAxisType());
        }
    }

    public abstract Axis.Type xAxisType();

    public abstract Axis.Type yAxisType();

    public GOptions getOptions() {
        return options;
    }

    public double xUnscale(double x) {
        return plot.xUnscale(x);
    }

    public double xScale(double x) {
        return plot.xScale(x);
    }

    public double yUnscale(double y) {
        return plot.yUnscale(y);
    }

    public double yScale(double y) {
        return plot.yScale(y);
    }

    public boolean contains(double x, double y) {
        return plot.xAxis().contains(x) && plot.yAxis().contains(y);
    }

    public boolean union(double x, double y) {
        if (Double.isFinite(x) && Double.isNaN(y)) {
            return plot.xAxis().unionNumeric(x);
        }
        if (Double.isNaN(x) && Double.isFinite(y)) {
            return plot.yAxis().unionNumeric(y);
        }
        if (plot.xAxis().allowUnion(x) && plot.yAxis().allowUnion(y)) {
            plot.xAxis().unionNumeric(x);
            plot.yAxis().unionNumeric(y);
            return true;
        }
        return false;
    }

    public abstract void updateDataRange(Graphics2D g2d);

    public abstract void paint(Graphics2D g2d);
}
