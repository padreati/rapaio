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
import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.plot.PlotComponent;

import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
public class Plot extends BaseFigure {

    private final List<PlotComponent> components;

    public Plot() {
        components = new LinkedList<>();
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

        if (opt().getXRangeStart() == opt().getXRangeStart() && opt().getXRangeEnd() == opt().getXRangeEnd()) {
            r.setX1(opt().getXRangeStart());
            r.setX2(opt().getXRangeEnd());
        }
        if (opt().getYRangeStart() == opt().getYRangeStart() && opt().getYRangeEnd() == opt().getYRangeEnd()) {
            r.setY1(opt().getYRangeStart());
            r.setY2(opt().getYRangeEnd());
        }

        if(r.getY1()==r.getY2()) {
            r.setY1(r.getY1()-0.5);
            r.setY2(r.getY2()+0.5);
        }
        return r;
    }
    
    public List<PlotComponent> getComponents() {
        return components;
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
