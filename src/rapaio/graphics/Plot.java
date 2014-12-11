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

import rapaio.graphics.base.HostFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
public class Plot extends HostFigure {

    private final List<PlotComponent> components = new LinkedList<>();

    public Plot() {
        bottomThick(true);
        bottomMarkers(true);
        leftThick(true);
        leftMarkers(true);
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

    public Plot add(PlotComponent pc) {
        pc.initialize(this);
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
    protected void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    protected void buildBottomMarkers() {
        buildNumericBottomMarkers();
    }
}
