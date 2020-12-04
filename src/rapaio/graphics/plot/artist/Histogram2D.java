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

import rapaio.data.Var;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionBins;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/5/14.
 */
public class Histogram2D extends Artist {

    private static final long serialVersionUID = 136436073834179971L;

    private final Var x;
    private final Var y;
    private int[][] freq;
    private int maxFreq;

    public Histogram2D(Var x, Var y, GOption<?>... opts) {
        this.x = x;
        this.y = y;
        this.options.bind(opts);
        this.options.setBins(new GOptionBins(10));
        options.bind(opts);
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
        parent.leftMarkers(true);
        parent.leftThick(true);
        parent.xLab(x.name());
        parent.bottomMarkers(true);
        parent.bottomThick(true);
        parent.yLab(y.name());
    }

    private void computeData() {
        int bins = options.getBins();
        double w = plot.xAxis().length() / bins;
        double h = plot.yAxis().length() / bins;

        freq = new int[bins][bins];

        for (int i = 0; i < Math.min(x.size(), y.size()); i++) {
            if (x.isMissing(i) || y.isMissing(i))
                continue;

            int xx = Math.min(bins - 1, (int) Math.floor((x.getDouble(i) - plot.xAxis().min()) / w));
            int yy = Math.min(bins - 1, (int) Math.floor((y.getDouble(i) - plot.yAxis().min()) / h));
            freq[xx][yy]++;
            if (maxFreq < freq[xx][yy]) {
                maxFreq = freq[xx][yy];
            }
        }
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        if (x.size() == 0) {
            return;
        }
        for (int i = 0; i < Math.min(x.size(), y.size()); i++) {
            if (x.isMissing(i) || y.isMissing(i)) {
                continue;
            }
            union(x.getDouble(i), y.getDouble(i));
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        computeData();
        // paint each rectangle as a blue gradient

        int bins = options.getBins();

        double w = plot.xAxis().length() / bins;
        double h = plot.yAxis().length() / bins;

        for (int i = 0; i < bins; i++) {
            for (int j = 0; j < bins; j++) {
                int blue = (int) (255 * freq[i][j] / (1.0 * maxFreq));
                Color c = options.getFill(0);
                Color color = new Color(c.getRed(), c.getGreen(), c.getBlue(), blue);
                g2d.setColor(color);
                Rectangle2D.Double rr = new Rectangle2D.Double(
                        xScale(plot.xAxis().min() + w * i),
                        yScale(plot.yAxis().min() + h * j + h),
                        xScale(plot.xAxis().min() + w * i + w) - xScale(plot.xAxis().min() + w * i),
                        yScale(plot.yAxis().min() + h * j) - yScale(plot.yAxis().min() + h * j + h));
                g2d.fill(rr);
            }
        }
    }
}
