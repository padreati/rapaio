/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.graphics.plot.artist;

import static rapaio.util.time.PrettyTimeInterval._1_DAY;
import static rapaio.util.time.PrettyTimeInterval._1_HOUR;
import static rapaio.util.time.PrettyTimeInterval._1_MONTH;
import static rapaio.util.time.PrettyTimeInterval._1_YEAR;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import rapaio.finance.data.FinBar;
import rapaio.finance.data.FinBarSize;
import rapaio.graphics.opt.GOption;
import rapaio.graphics.opt.GOptionFill;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.plot.Artist;
import rapaio.graphics.plot.Axis;
import rapaio.graphics.plot.Plot;

public class CandlestickChart extends Artist {

    private final List<FinBar> bars;
    private final FinBarSize barSize;

    public CandlestickChart(List<FinBar> bars, FinBarSize barSize, GOption<?>... opts) {
        this.bars = bars;
        this.barSize = barSize;

        // default values can stay here, before general bind
        options.setFill(new GOptionFill(
                NColor.tab_orange, // state 0
                NColor.tab_green, // state 1
                NColor.tab_red // state -1
        ));
        options.bind(opts);
    }

    @Override
    public Axis.Type xAxisType() {
        return Axis.Type.newDiscreteTime();
    }

    @Override
    public Axis.Type yAxisType() {
        return Axis.Type.newNumeric();
    }

    @Override
    public void bind(Plot parent) {
        super.bind(parent);

        // here we can override thins in parent
    }

    @Override
    public void updateDataRange(Graphics2D g2d) {
        for (FinBar bar : bars) {
            plot.yAxis().domain().unionNumeric(bar.low());
            plot.yAxis().domain().unionNumeric(bar.high());
            plot.xAxis().domain().unionDiscreteTime(bar.time());
        }
    }

    @Override
    public void paint(Graphics2D g2d) {
        if (plot.xAxis().type() instanceof Axis.TypeDiscreteTime dt) {
            Map<Instant, Double> innerMap = dt.computeInnerMap(plot.xAxis());
            drawSeparators(g2d, innerMap);
            drawCandles(g2d, innerMap);
        }
    }

    private void drawSeparators(Graphics2D g2d, Map<Instant, Double> innerMap) {
        Graphics2D g = (Graphics2D) g2d.create();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        Instant[] instants = new Instant[innerMap.size()];
        for (Map.Entry<Instant, Double> entry : innerMap.entrySet()) {
            instants[entry.getValue().intValue()] = entry.getKey();
        }

        boolean[] yearMarker = new boolean[instants.length];
        boolean[] monthMarker = new boolean[instants.length];
        boolean[] dayMarker = new boolean[instants.length];
        boolean[] hourMarker = new boolean[instants.length];
        int yearMarkerCount = 0;
        int monthMarkerCount = 0;
        int dayMarkerCount = 0;
        int hourMarkerCount = 0;

        for (int i = 1; i < instants.length; i++) {
            Instant before = instants[i - 1];
            Instant after = instants[i];

            if (!_1_YEAR.getInstantBefore(before).equals(_1_YEAR.getInstantBefore(after))) {
                yearMarker[i] = true;
                yearMarkerCount++;
            }
            if (!_1_MONTH.getInstantBefore(before).equals(_1_MONTH.getInstantBefore(after))) {
                monthMarker[i] = true;
                monthMarkerCount++;
            }
            if (!_1_DAY.getInstantBefore(before).equals(_1_DAY.getInstantBefore(after))) {
                dayMarker[i] = true;
                dayMarkerCount++;
            }
            if (!_1_HOUR.getInstantBefore(before).equals(_1_HOUR.getInstantBefore(after))) {
                hourMarker[i] = true;
                hourMarkerCount++;
            }
        }

        boolean[] mainMarker = null;
        boolean[] secondaryMarker = null;

        if (yearMarkerCount > 0) {
            mainMarker = yearMarker;
            secondaryMarker = monthMarker;
        } else if (monthMarkerCount > 0) {
            mainMarker = monthMarker;
            secondaryMarker = dayMarker;
        } else if (dayMarkerCount > 0) {
            mainMarker = dayMarker;
            secondaryMarker = hourMarker;
        } else if (hourMarkerCount > 0) {
            mainMarker = hourMarker;
        }

        for (int i = 0; i < instants.length; i++) {

            if (mainMarker != null && mainMarker[i]) {
                // draw main marker
                Stroke dashed = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {2f, 0f, 2f}, 2f);
                g.setStroke(dashed);
                g.setColor(NColor.darkgray);
                g.drawLine((int) xScale(i), (int) yScale(plot.yAxis().min()),
                        (int) xScale(i),
                        (int) (yScale(plot.yAxis().max()) - yScale(plot.yAxis().min())));
            }

            if (secondaryMarker != null && secondaryMarker[i]) {
                // draw secondary marker
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, new float[] {2f, 0f, 2f}, 2f);
                g.setStroke(dashed);
                g.setColor(NColor.lightgray);
                g.drawLine((int) xScale(i), (int) yScale(plot.yAxis().min()),
                        (int) xScale(i),
                        (int) (yScale(plot.yAxis().max()) - yScale(plot.yAxis().min())));
            }
        }

        g.dispose();
    }

    private void drawCandles(Graphics2D g2d, Map<Instant, Double> innerMap) {
        Composite old = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, options.getAlpha()));

        g2d.setStroke(new BasicStroke(options.getLwd()));
        for (FinBar bar : bars) {
            Instant time = bar.time();
            Double value = innerMap.get(time);
            if (value == null) {
                continue;
            }

            double xIntervalBegin = value;
            double xIntervalEnd = value + 1;

            // we keep a padding of 0.2
            double xStart = xScale(xIntervalBegin + 0.1);
            double xEnd = xScale(xIntervalEnd - 0.1);

            double bodyLow = yScale(Math.min(bar.open(), bar.close()));
            double bodyHigh = yScale(Math.max(bar.open(), bar.close()));

            double start = xScale(xIntervalBegin + 0.45);
            double end = xScale(xIntervalEnd - 0.45);

            double lwd = end - start;

            double low = yScale(bar.low());
            double high = yScale(bar.high());

            int state = 0;
            if (bar.open() < bar.close()) {
                state = 1;
            }
            if (bar.open() > bar.close()) {
                state = 2;
            }

            g2d.setColor(options.getFill(state));
            g2d.fill(new Rectangle2D.Double(start, high, lwd, low - high));
            g2d.fill(new Rectangle2D.Double(xStart, low, xEnd - xStart, lwd));
            g2d.fill(new Rectangle2D.Double(xStart, high, xEnd - xStart, lwd));
            g2d.fill(new Rectangle2D.Double(xStart, bodyHigh, xEnd - xStart, bodyLow - bodyHigh));

        }

        g2d.setComposite(old);
    }
}
