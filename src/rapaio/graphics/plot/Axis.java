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

package rapaio.graphics.plot;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import rapaio.graphics.base.XWilkinson;

/**
 * @author padreati
 */
public final class Axis implements Serializable {

    @Serial
    private static final long serialVersionUID = 8011268159946315468L;

    public enum Type {
        UNKNOWN,
        NUMERIC,
        CATEGORY,
        INSTANT
    }

    private static final double EXTENDED_FACTOR = 1.05;

    private Type type = Type.UNKNOWN;

    private final List<Double> tickers = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();

    private double hardMin = Double.NaN;
    private double hardMax = Double.NaN;
    private double min = Double.NaN;
    private double max = Double.NaN;

    private final Map<String, Double> categoryMap = new HashMap<>();

    public void clear() {
        min = Double.NaN;
        max = Double.NaN;
        tickers.clear();
        labels.clear();
    }

    public void hardLim(double hardMin, double hardMax) {
        if (Double.isFinite(hardMin)) {
            this.hardMin = hardMin;
        }
        if (Double.isFinite(hardMax)) {
            this.hardMax = hardMax;
        }
    }

    public boolean allowUnion(double x) {
        return Double.isFinite(x)
                && (!Double.isFinite(hardMin) || (x >= hardMin))
                && (!Double.isFinite(hardMax) || (x <= hardMax));
    }

    public boolean unionNumeric(double x) {
        if (!allowUnion(x)) {
            return false;
        }
        min = (Double.isFinite(min)) ? Math.min(min, x) : x;
        max = (Double.isFinite(max)) ? Math.max(max, x) : x;
        return true;
    }

    public boolean unionCategory(double x, String label) {
        if (!allowUnion(x)) {
            return false;
        }
        unionNumeric(x);
        categoryMap.put(label, x);
        return true;
    }

    public void computeArtifacts(Plot plot, double viewportSpan) {
        if (!Double.isFinite(min)) {
            min = 0;
        }
        if (!Double.isFinite(max)) {
            max = 1;
        }
        min = Double.isFinite(hardMin) ? hardMin : min;
        max = Double.isFinite(hardMax) ? hardMax : max;
        if (min == max) {
            min = min - 0.5;
            max = max + 0.5;
        }
        extendedRange();
        buildMarkers(plot, viewportSpan);
    }

    public void extendedRange() {
        // no extension for instance type
        if (type == Type.INSTANT) {
            return;
        }
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

        int spots = (int) Math.floor(span / plot.thickerMinSpace) + 1;
        if (spots < 2) {
            return;
        }

        switch (type) {
            case CATEGORY -> computeCategoryMarkers(span, spots);
            case INSTANT -> computeInstantMarkers(span, spots);
            default -> computeNumericMarkers(span, spots);
        }
    }

    private void computeCategoryMarkers(double span, int spots) {
        for (Map.Entry<String, Double> entry : categoryMap.entrySet()) {
            tickers.add((entry.getValue() - min) * span / length());
            labels.add(entry.getKey());
        }
    }

    private void computeInstantMarkers(double span, int spots) {

        XWilkinson.Labels xlabels;
        SimpleDateFormat sdf;

        long range = (long) (max - min + 1);
        Duration duration = Duration.of(range, ChronoUnit.MILLIS);
        if (duration.toDays() > 2 * 365) {
            xlabels = XWilkinson.forYears(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy");
        } else if (duration.toDays() > 2 * 30) {
            xlabels = XWilkinson.forMonths(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy.MM");
        } else if (duration.toDays() > 2) {
            xlabels = XWilkinson.forDays(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy.MM.dd");
        } else if (duration.toMinutes() > 2) {
            xlabels = XWilkinson.forHours24(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        } else if (duration.toSeconds() > 2) {
            xlabels = XWilkinson.forSeconds(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        } else {
            xlabels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
            sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        }

        for (double value : xlabels.getList()) {
            tickers.add((value - min) * span / length());
            labels.add(sdf.format(Date.from(Instant.ofEpochMilli((long) value))));
        }

    }

    private void computeNumericMarkers(double span, int spots) {
        XWilkinson.Labels numLabels = XWilkinson.base10(XWilkinson.DEEFAULT_EPS).searchBounded(min, max, spots);
        for (double label : numLabels.getList()) {
            tickers.add((label - min) * span / length());
            labels.add(numLabels.getFormattedValue(label));
        }
    }

    public String toString() {
        return "Axis{min=" + min + ", max=" + max + ", tickers=" + tickers.stream().map(String::valueOf).collect(Collectors.joining(","))
                + ", labels=" + String.join(",", labels) + '}';
    }
}
