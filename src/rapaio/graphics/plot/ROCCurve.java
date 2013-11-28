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
import rapaio.graphics.Plot;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurve extends PlotComponent {

    private final ROC roc;

    public ROCCurve(Plot plot, ROC roc) {
        super(plot);
        this.roc = roc;
        plot.setBottomLabel("fp rate");
        plot.setLeftLabel("tp rate");
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

        for (int i = 1; i < roc.getData().getRowCount(); i++) {
            g2d.setColor(opt().getColor(i));
            double x1 = plot.xscaledbl(roc.getData().getValue(i - 1, "fpr"));
            double y1 = plot.yscaledbl(roc.getData().getValue(i - 1, "tpr"));
            double x2 = plot.xscaledbl(roc.getData().getValue(i, "fpr"));
            double y2 = plot.yscaledbl(roc.getData().getValue(i, "tpr"));

            if (plot.getRange().contains(roc.getData().getValue(i - 1, "fpr"), roc.getData().getValue(i - 1, "tpr"))
                    && plot.getRange().contains(roc.getData().getValue(i, "fpr"), roc.getData().getValue(i, "tpr"))) {
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }

        g2d.setColor(opt().getColor(0));
    }
}
