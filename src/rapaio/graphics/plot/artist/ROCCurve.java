/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.graphics.plot.artist;

import rapaio.graphics.opt.GOption;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;
import rapaio.ml.eval.metric.ROC;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class ROCCurve extends Artist {

    private static final long serialVersionUID = 4110642211338491615L;
    private final ROC roc;

    public ROCCurve(ROC roc, GOption<?>... opts) {
        this.roc = roc;
        this.options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.NUMERIC;
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);
        parent.xLab("fp rate");
        parent.yLab("tp rate");
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        union(0, 0);
        union(1, 1);
    }

    @Override
    public void paint(Graphics2D g2d) {
        g2d.setColor(options.getColor(0));
        g2d.setStroke(new BasicStroke(options.getLwd()));

        for (int i = 1; i < roc.data().rowCount(); i++) {
            g2d.setColor(options.getFill(i));
            double x1 = xScale(roc.data().getDouble(i - 1, "fpr"));
            double y1 = yScale(roc.data().getDouble(i - 1, "tpr"));
            double x2 = xScale(roc.data().getDouble(i, "fpr"));
            double y2 = yScale(roc.data().getDouble(i, "tpr"));

            var data = roc.data();
            if (contains(data.getDouble(i - 1, "fpr"), data.getDouble(i - 1, "tpr"))
                    && contains(data.getDouble(i, "fpr"), data.getDouble(i, "tpr"))) {
                g2d.draw(new Line2D.Double(x1, y1, x2, y2));
            }
        }
    }
}
