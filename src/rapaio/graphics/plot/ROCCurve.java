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

package rapaio.graphics.plot;

import rapaio.core.stat.ROC;
import rapaio.data.IndexVector;
import rapaio.data.RowComparators;
import rapaio.data.Vector;
import rapaio.filters.RowFilters;
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;
import rapaio.graphics.pch.PchPalette;

import java.awt.*;
import java.util.*;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurve extends PlotComponent {

    private final ROC roc;

    public ROCCurve(Plot rocPlot, ROC roc) {
        super(rocPlot);
        this.roc = roc;
    }


    @Override
    public Range getComponentDataRange() {
        return new Range(0, 0, 1, 1);
    }

    @Override
    public void paint(Graphics2D g2d) {

        g2d.setColor(opt().getColor(0));
        float lwd = opt().getLwd();
        g2d.setStroke(new BasicStroke(lwd));
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 1; i < roc.getFPRateVector().getRowCount(); i++) {
            g2d.setColor(opt().getColor(i));
            int x1 = (plot.xscale(roc.getFPRateVector().getValue(i - 1)));
            int y1 = (plot.yscale(roc.getTPRateVector().getValue(i - 1)));
            int x2 = (plot.xscale(roc.getFPRateVector().getValue(i)));
            int y2 = (plot.yscale(roc.getTPRateVector().getValue(i)));

            if (plot.getRange().contains(roc.getFPRateVector().getValue(i - 1), roc.getTPRateVector().getValue(i - 1))
                    && plot.getRange().contains(roc.getFPRateVector().getValue(i), roc.getTPRateVector().getValue(i))) {
                g2d.drawLine(x1, y1, x2, y2);
            }
        }

        g2d.setColor(opt().getColor(0));
        int xx = plot.xscale(roc.getBestAccFPRate());
        int yy = plot.yscale(roc.getBestAccTPRate());
        PchPalette.STANDARD.draw(g2d, xx, yy, opt().getSize(0), opt().getPch(0));
    }
}
