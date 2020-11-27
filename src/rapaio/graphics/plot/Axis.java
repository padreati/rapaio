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

package rapaio.graphics.plot;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public final class Axis implements Serializable {

    private static final long serialVersionUID = 8011268159946315468L;

    public enum Type {
        UNKNOWN,
        NUMERIC,
        CATEGORY
    }

    private static final double EXTENDED_FACTOR = 1.05;

    private Type type = Type.UNKNOWN;

    private final List<Double> tickers = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();

    private double min;
    private double max;

    private final Map<String, Double> categoryMap = new HashMap<>();

    public void clear() {
        min = Double.NaN;
        max = Double.NaN;
        tickers.clear();
        labels.clear();
    }

    public void unionNumeric(double x) {
        if (Double.isFinite(x)) {
            min = (Double.isFinite(min)) ? Math.min(min, x) : x;
            max = (Double.isFinite(max)) ? Math.max(max, x) : x;
        }
    }

    public void unionCategory(double x, String label) {
        unionNumeric(x);
        categoryMap.put(label, x);
    }

    public void computeArtifacts(Plot plot, double viewportSpan, double limStart, double limEnd) {
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
        buildMarkers(plot, viewportSpan);
    }

    public void extendedRange() {
        double extRange = Math.abs(max - min) * EXTENDED_FACTOR;
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

    public Type type() {
        return type;
    }

    public void type(Type type) {
        this.type = type;
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

    public void buildMarkers(Plot plot, double span) {
        tickers.clear();
        labels.clear();

        double rangeLength = length();

        int spots = (int) Math.floor(span / plot.thickerMinSpace) + 1;
        if (spots < 2) {
            return;
        }

        if (categoryMap.isEmpty()) {
            XWilkinson.Labels labels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);

            for (double label : labels.getList()) {
                tickers.add((label - min) * span / length());
                this.labels.add(labels.getFormattedValue(label));
            }
            return;
        }

        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            tickers.add((entry.getValue() - min) * span / length());
            labels.add(entry.getKey());
        }
    }

    public String toString() {
        return "Axis{min=" + min + ", max=" + max +
                ", tickers=" + tickers.stream().map(String::valueOf).collect(Collectors.joining()) +
                ", labels=" + String.join(",", labels) +
                '}';
    }
}
