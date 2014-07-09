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

import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.plot.PlotComponent;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
@Deprecated
public class Plot extends BaseFigure {

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

        if (getXRangeStart() == getXRangeStart() && getXRangeEnd() == getXRangeEnd()) {
            range.setX1(getXRangeStart());
            range.setX2(getXRangeEnd());
        }
        if (getYRangeStart() == getYRangeStart() && getYRangeEnd() == getYRangeEnd()) {
            range.setY1(getYRangeStart());
            range.setY2(getYRangeEnd());
        }

        if (range.getY1() == range.getY2()) {
            range.setY1(range.getY1() - 0.5);
            range.setY2(range.getY2() + 0.5);
        }
        return range;
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
    protected void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    protected void buildBottomMarkers() {
        buildNumericBottomMarkers();
    }


}
