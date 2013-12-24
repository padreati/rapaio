/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
package rapaio.graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import rapaio.graphics.base.AbstractFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.plot.PlotComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
public class Plot extends AbstractFigure {

    private final List<PlotComponent> components = new LinkedList<>();

    public Plot() {
        setBottomThicker(true);
        setBottomMarkers(true);
        setLeftThicker(true);
        setLeftMarkers(true);
    }
    
    @Override
    public Range buildRange() {
        Range r = null;
        for (PlotComponent pc : components) {
            Range newrange = pc.getRange();
            if (newrange != null) {
                if (r == null) {
                    r = newrange;
                } else {
                    r.union(newrange);
                }
            }
        }

        if (r == null) {
            r = new Range(0, 0, 1, 1);
        }

        if (getXRangeStart() == getXRangeStart() && getXRangeEnd() == getXRangeEnd()) {
            r.setX1(getXRangeStart());
            r.setX2(getXRangeEnd());
        }
        if (getYRangeStart() == getYRangeStart() && getYRangeEnd() == getYRangeEnd()) {
            r.setY1(getYRangeStart());
            r.setY2(getYRangeEnd());
        }

        if (r.getY1() == r.getY2()) {
            r.setY1(r.getY1() - 0.5);
            r.setY2(r.getY2() + 0.5);
        }
        return r;
    }

    public Plot add(PlotComponent pc) {
        pc.setParent(this);
        pc.initialize();
        components.add(pc);
        return this;
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

    @Override
    public Plot setXRange(double start, double end) {
        super.setXRange(start, end);
        return this;
    }

    @Override
    public Plot setYRange(double start, double end) {
        super.setYRange(start, end);
        return this;
    }
}
