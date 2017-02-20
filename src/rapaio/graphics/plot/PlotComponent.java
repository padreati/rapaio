/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.graphics.base.BaseFigure;

import java.awt.*;

/**
 * @author Aurelian Tutuianu
 */
public abstract class PlotComponent extends BaseFigure {

    private static final long serialVersionUID = -797168275849511614L;
    protected Plot parent;

    public void initialize(Plot parent) {
        if (parent == null) {
            throw new IllegalArgumentException("parent plot reference is null");
        }
        this.parent = parent;
        this.options.setParent(parent.getOptions());
    }

    public double xScale(double x) {
        return parent.xScale(x);
    }

    public double yScale(double y) {
        return parent.yScale(y);
    }

    public abstract void paint(Graphics2D g2d);
}
