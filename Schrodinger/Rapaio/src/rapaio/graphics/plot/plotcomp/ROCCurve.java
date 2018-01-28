/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

package rapaio.graphics.plot.plotcomp;

import rapaio.graphics.base.Range;
import rapaio.graphics.opt.ColorPalette;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.PlotComponent;
import rapaio.ml.eval.ROC;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurve extends PlotComponent {

    private static final long serialVersionUID = 4110642211338491615L;
    private final ROC roc;

    public ROCCurve(ROC roc, GOption... opts) {
        this.roc = roc;
        this.options.bind(opts);
    }

    @Override
    public Range buildRange() {
        return new Range(0, 0, 1, 1);
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);
        parent.xLab("fp rate");
        parent.yLab("tp rate");
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(options.getColor(0));
        g2d.setStroke(new BasicStroke(options.getLwd()));
        g2d.setBackground(ColorPalette.STANDARD.getColor(255));

        for (int i = 1; i < roc.data().rowCount(); i++) {
            g2d.setColor(options.getColor(i));
            double x1 = parent.xScale(roc.data().value(i - 1, "fpr"));
            double y1 = parent.yScale(roc.data().value(i - 1, "tpr"));
            double x2 = parent.xScale(roc.data().value(i, "fpr"));
            double y2 = parent.yScale(roc.data().value(i, "tpr"));

            if (parent.getRange().contains(
                    roc.data().value(i - 1, "fpr"),
                    roc.data().value(i - 1, "tpr")
            )
                    && parent.getRange().contains(
                    roc.data().value(i, "fpr"),
                    roc.data().value(i, "tpr"))) {
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }

        g2d.setColor(options.getColor(0));
    }
}
