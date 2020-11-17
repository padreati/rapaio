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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.graphics.plot;

import rapaio.graphics.base.XWilkinson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author algoshipda
 */
public abstract class Axis implements Serializable {

    public static Axis numeric(Plot plot, double min, double max) {
        return new NumericAxis(plot, min, max);
    }

    public static Axis numeric(Plot plot) {
        return new NumericAxis(plot, Double.NaN, Double.NaN);
    }

    public static Axis nominal(Plot plot) {
        return new NominalAxis(plot);
    }

    private static final long serialVersionUID = 8011268159946315468L;
    protected static final double EXTENDED_FACTOR = 1.025;

    protected final Plot plot;

    protected double min;
    protected double max;
    protected final List<Double> tickers = new ArrayList<>();
    protected final List<String> labels = new ArrayList<>();

    public Axis(Plot plot, double min, double max) {
        this.plot = plot;
        this.min = min;
        this.max = max;
    }

    public void clear() {
        min = Double.NaN;
        max = Double.NaN;
        tickers.clear();
        labels.clear();
    }

    public abstract void unionNumeric(double x);

    public abstract void unionCategory(double x, String label);

    public void computeArtifacts(double viewportSpan, double limStart, double limEnd) {
        if (!Double.isFinite(min)) {
            min = 0;
        }
        if (!Double.isFinite(max)) {
            max = 1;
        }
        if (Double.isFinite(limStart * limEnd)) {
            min = limStart;
            max = limEnd;
        }
        if (min == max) {
            min = min - 0.5;
            max = max + 0.5;
        }
        extendedRange();
        buildMarkers(viewportSpan);
    }

    public void extendedRange() {
        double extRange = (max - min) * EXTENDED_FACTOR;
        double mid = min + (max - min) / 2;
        if (min == max) {
            min = min - 1;
            max = max + 1;
        } else {
            min = mid - extRange / 2;
            max = mid + extRange / 2;
        }
    }

    public boolean contains(double x) {
        return min <= x && x <= max;
    }

    public double min() {
        return min;
    }

    public void min(double value) {
        min = value;
    }

    public double max() {
        return max;
    }

    public void max(double value) {
        max = value;
    }

    public double length() {
        return max - min;
    }

    public List<Double> tickers() {
        return tickers;
    }

    public List<String> labels() {
        return labels;
    }

    public abstract void buildMarkers(double span);

    private static class NumericAxis extends Axis {
        private static final long serialVersionUID = 2366386317046631301L;

        public NumericAxis(Plot plot, double min, double max) {
            super(plot, min, max);

        }

        @Override
        public void unionNumeric(double x) {
            if (Double.isFinite(x)) {
                min = (Double.isFinite(min)) ? Math.min(min, x) : x;
                max = (Double.isFinite(max)) ? Math.max(max, x) : x;
            }
        }

        @Override
        public void unionCategory(double x, String label) {
            unionNumeric(x);
        }

        @Override
        public void buildMarkers(double span) {
            tickers.clear();
            labels.clear();

            int spots = (int) Math.floor(span / plot.thickerMinSpace) + 1;
            if (spots < 2) {
                return;
            }
            XWilkinson.Labels labels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);

            for (double label : labels.getList()) {
                tickers.add((label - min) * span / length());
                this.labels.add(labels.getFormattedValue(label));
            }
        }

        @Override
        public String toString() {
            return "NumericAxis{" +
                    "min=" + min +
                    ", max=" + max +
                    ", tickers=" + tickers.stream().map(String::valueOf).collect(Collectors.joining()) +
                    ", labels=" + String.join(",", labels) +
                    '}';
        }
    }

    private static class NominalAxis extends Axis {
        private static final long serialVersionUID = 2366386317046631301L;

        private final Map<String, Double> categories = new HashMap<>();

        public NominalAxis(Plot plot) {
            super(plot, Double.NaN, Double.NaN);
        }

        @Override
        public void clear() {
            super.clear();
            categories.clear();
        }

        @Override
        public void unionNumeric(double x) {
            if (Double.isFinite(x)) {
                min = (Double.isFinite(min)) ? Math.min(min, x) : x;
                max = (Double.isFinite(max)) ? Math.max(max, x) : x;
            }
        }

        @Override
        public void unionCategory(double x, String label) {
            unionNumeric(x);
            categories.put(label, x);
        }

        @Override
        public void buildMarkers(double span) {
            tickers.clear();
            labels.clear();

            double rangeLength = length();

            int spots = (int) Math.floor(span / plot.thickerMinSpace) + 1;
            if (spots < 2) {
                return;
            }

            if (categories.isEmpty()) {
                XWilkinson.Labels labels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);

                for (double label : labels.getList()) {
                    tickers.add((label - min) * span / length());
                    this.labels.add(labels.getFormattedValue(label));
                }
                return;
            }

            for (Map.Entry<String, Double> entry : categories.entrySet()) {
                tickers.add((entry.getValue() - min) * span / length());
                labels.add(entry.getKey());
            }
        }

        @Override
        public String toString() {
            return "NumericAxis{" +
                    "min=" + min +
                    ", max=" + max +
                    ", tickers=" + tickers.stream().map(String::valueOf).collect(Collectors.joining()) +
                    ", labels=" + String.join(",", labels) +
                    '}';
        }
    }
}
