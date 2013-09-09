/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.core.stat.Sum;
import rapaio.data.OneIndexVector;
import rapaio.data.IndexVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.graphics.base.BaseFigure;
import rapaio.graphics.base.Range;
import rapaio.graphics.colors.ColorPalette;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class BarChart extends BaseFigure {

    private final Vector numeric;
    private final Vector nominal;
    private double[] values;
    private Range range;

    public BarChart(Vector nominal) {
        this(nominal, new IndexVector("count", nominal.getRowCount(), 1));
    }

    public BarChart(Vector nominal, Vector numeric) {
        if (!nominal.isNominal()) {
            throw new IllegalArgumentException("Nominal vector must be ... isNominal");
        }
        if (!numeric.isNumeric()) {
            throw new IllegalArgumentException("Numeric vector must be .. isNumeric");
        }

        this.numeric = numeric;
        this.nominal = nominal;

        leftThicker = true;
        leftMarkers = true;
        bottomThicker = true;
        bottomMarkers = true;

        getOp().setColorIndex(new OneIndexVector(7));

        this.setLeftLabel(numeric.getName());
        this.setBottomLabel(nominal.getName());
    }

    @Override
    public Range buildRange() {
        if (range == null) {

            // build preliminaries
            int bins = nominal.dictionary().length;
            values = new double[bins];
            HashMap<String, ArrayList<Double>> map = new HashMap<>();
            for (String label : nominal.dictionary()) {
                map.put(label, new ArrayList<Double>());
            }
            for (int i = 0; i < numeric.getRowCount(); i++) {
                map.get(nominal.getLabel(i)).add(numeric.getValue(i));
            }
            for (int i = 0; i < values.length; i++) {
                Vector v = new NumericVector("", map.get(nominal.dictionary()[i]).size());
                for (int j = 0; j < v.getRowCount(); j++) {
                    v.setValue(j, map.get(nominal.dictionary()[i]).get(j));
                }
                values[i] = new Sum(v).getValue();
            }

            // now build range
            range = new Range();
            for (int i = 0; i < values.length; i++) {
                range.union(i, values[i]);
            }
            range.union(-0.5, 0);
            range.union(values.length - 0.5, 0);
        }
        return range;
    }

    @Override
    public void buildLeftMarkers() {
        buildNumericLeftMarkers();
    }

    @Override
    public void buildBottomMarkers() {
        bottomMarkersPos.clear();
        bottomMarkersMsg.clear();

        int xspots = nominal.dictionary().length;
        double xspotwidth = viewport.width / xspots;

        for (int i = 0; i < xspots; i++) {
            bottomMarkersPos.add(xspotwidth / 2 + i * xspotwidth);
            bottomMarkersMsg.add(nominal.dictionary()[i]);
        }
    }

    @Override
    public void paint(Graphics2D g2d, Rectangle rect) {
        super.paint(g2d, rect);

        for (int i = 0; i < values.length; i++) {
            int[] x = {xscale(i - 0.4), xscale(i - 0.4), xscale(i + 0.4), xscale(i + 0.4), xscale(i - 0.4)};
            int[] y = {yscale(0), yscale(values[i]), yscale(values[i]), yscale(0), yscale(0)};
            g2d.setColor(ColorPalette.STANDARD.getColor(0));
            g2d.drawPolygon(x, y, 4);
            x = new int[]{xscale(i - 0.4) + 1, xscale(i - 0.4) + 1, xscale(i + 0.4), xscale(i + 0.4), xscale(i - 0.4) + 1};
            y = new int[]{yscale(0), yscale(values[i]) + 1, yscale(values[i]) + 1, yscale(0), yscale(0)};
            g2d.setColor(getOp().getColor(i));
            g2d.fillPolygon(x, y, 4);
        }
    }
}
