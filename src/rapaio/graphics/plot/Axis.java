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

import java.awt.Graphics2D;
import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import rapaio.graphics.base.XWilkinson;
import rapaio.util.time.PrettyTimeInterval;

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
        INSTANT,
        DISCRETE_INSTANT
    }

    private static final double EXTENDED_FACTOR = 1.05;

    private Type type = Type.UNKNOWN;
    private final Domain domain = new Domain();
    private final List<Double> tickers = new ArrayList<>();
    private final List<String> labels = new ArrayList<>();
    private double min = Double.NaN;
    private double max = Double.NaN;

    public void clear() {
        min = Double.NaN;
        max = Double.NaN;
        domain.clearData();
        tickers.clear();
        labels.clear();
    }

    public Domain domain() {
        return domain;
    }

    public void computeArtifacts(Plot plot, Graphics2D g2d, double viewportSpan) {

        InnerValues values = domain.computeInnerValues();
        min = values.min;
        max = values.max;

        // we do that since hard limits are used both to cut an eventual longer
        // interval, but also to extend one if it is shorter
        min = Double.isFinite(domain.hardMin) ? domain.hardMin : min;
        max = Double.isFinite(domain.hardMax) ? domain.hardMax : max;
        if (min == max) {
            min = min - 0.5;
            max = max + 0.5;
        }
        extendedRange();
        buildMarkers(plot, g2d, viewportSpan);
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

    public double max() {
        return max;
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

    private void buildMarkers(Plot plot, Graphics2D g2d, double span) {
        tickers.clear();
        labels.clear();

        int spots = (int) Math.floor(span / plot.thickerMinSpace) + 1;
        if (spots < 2) {
            return;
        }

        switch (type) {
            case CATEGORY -> computeCategoryMarkers(span);
            case INSTANT -> computeInstantMarkers(plot, g2d, span);
            case DISCRETE_INSTANT -> computeDiscreteInstantMarkers(plot, g2d, span, spots);
            default -> computeNumericMarkers(span, spots);
        }
    }

    private void computeCategoryMarkers(double span) {
        for (Map.Entry<String, Domain.CategoryInterval> entry : domain.categoryValues.entrySet()) {
            tickers.add((entry.getValue().value - min) * span / length());
            labels.add(entry.getKey());
        }
    }

    /**
     * This is done to improve the search time since we know that for larger intervals there will be no
     * room to place labels for short time units.
     */
    private int minimumTimeIntervalIndex(Instant start, Instant end) {
        Duration duration = Duration.between(start, end);
        PrettyTimeInterval interval = PrettyTimeInterval._1_SEC;
        if (duration.toDays() >= 5) {
            interval = PrettyTimeInterval._4_HOUR;
        } else if (duration.toDays() >= 2) {
            interval = PrettyTimeInterval._15_MIN;
        } else if (duration.toHours() >= 2) {
            interval = PrettyTimeInterval._1_MIN;
        }
        PrettyTimeInterval[] intervals = PrettyTimeInterval.values();
        for (int i = 0; i < intervals.length; i++) {
            if (intervals[i].equals(interval)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Compute instant markers using the pretty time intervals.
     *
     * The strategy is to start from small pretty intervals towards the larger ones.
     * If the produced labels fit the screen, than we keep it as best candidate.
     *
     * If the produced labels are less than or equal with 2, than we go back one step
     * to the previous smaller interval and we use than
     */
    private void computeInstantMarkers(Plot plot, Graphics2D g2d, double span) {

        Instant start = Instant.ofEpochMilli((long) min);
        Instant end = Instant.ofEpochMilli((long) max);

        PrettyTimeInterval[] intervals = PrettyTimeInterval.values();
        int index = minimumTimeIntervalIndex(start, end);
        for (int i = index; i >= 0; i--) {
            PrettyTimeInterval interval = intervals[i];
            List<Instant> instants = interval.getInstantList(start, end);
            if (start.isAfter(instants.get(0))) {
                instants = instants.subList(1, instants.size());
            }
            if (end.isBefore(instants.get(instants.size() - 1))) {
                instants = instants.subList(0, instants.size() - 1);
            }
            double width = plot.getLabelFontMetrics(g2d, interval.groupFormat().format(Date.from(instants.get(0)))).getWidth() * 1.1;

            if (width * (instants.size() - 1) <= span) {
                List<Instant> selection = instants;
                if (instants.size() <= 2 && index != intervals.length - 1) {
                    // find previous
                    selection = selectInstantMarkersByElimination(span, width, intervals[i + 1]);
                    interval = intervals[i + 1];
                }
                // good to go, make labels
                for (Instant t : selection) {
                    tickers.add((t.toEpochMilli() - min) * span / length());
                    labels.add(interval.groupFormat().format(Date.from(t)));
                }
                return;
            }
        }
    }

    private List<Instant> selectInstantMarkersByElimination(double span, double width, PrettyTimeInterval interval) {
        Instant start = Instant.ofEpochMilli((long) min);
        Instant end = Instant.ofEpochMilli((long) max);
        List<Instant> instants = interval.getInstantList(start, end);
        if (start.isAfter(instants.get(0))) {
            instants = instants.subList(1, instants.size());
        }
        if (end.isBefore(instants.get(instants.size() - 1))) {
            instants = instants.subList(0, instants.size() - 1);
        }
        for (int period = 1; period < instants.size(); period++) {
            if (((instants.size() - 1) % period == 0) && (span > width * (instants.size() - 1) / period)) {
                List<Instant> selection = new ArrayList<>();
                for (int i = 0; i < instants.size(); i++) {
                    if (i % period == 0) {
                        selection.add(instants.get(i));
                    }
                }
                return selection;
            }
        }
        return instants;
    }

    private void computeDiscreteInstantMarkers(Plot plot, Graphics2D g2d, double span, int spots) {
        List<Instant> values = domain.instantDiscreteValues.stream().toList();

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

    public static class Domain {

        private final Set<Double> doubleValues = new HashSet<>();
        private final Map<CategoryInterval, String> categoryIndex = new HashMap<>();
        private final Map<String, CategoryInterval> categoryValues = new HashMap<>();
        private final Set<Instant> instantValues = new HashSet<>();
        private final Set<Instant> instantDiscreteValues = new HashSet<>();

        private double hardMin = Double.NaN;
        private double hardMax = Double.NaN;

        public void hardLim(double hardMin, double hardMax) {
            if (Double.isFinite(hardMin)) {
                this.hardMin = hardMin;
            }
            if (Double.isFinite(hardMax)) {
                this.hardMax = hardMax;
            }
        }

        private record CategoryInterval(double begin, double value, double end) {
            public boolean overlaps(CategoryInterval interval) {
                double maxBegin = Math.max(begin, interval.begin);
                double minEnd = Math.min(end, interval.end);
                return Math.abs(minEnd - maxBegin) > 1e-20;
            }
        }

        public void clearData() {
            doubleValues.clear();
            categoryIndex.clear();
            categoryValues.clear();
            instantValues.clear();
            instantDiscreteValues.clear();
        }

        public boolean allowUnion(double x) {
            return Double.isFinite(x) && (!Double.isFinite(hardMin) || (x >= hardMin)) && (!Double.isFinite(hardMax) || (x <= hardMax));
        }

        public boolean unionNumeric(double x) {
            if (!allowUnion(x)) {
                return false;
            }
            doubleValues.add(x);
            return true;
        }

        public boolean unionCategory(double begin, double index, double end, String label) {
            CategoryInterval interval = new CategoryInterval(begin, index, end);
            if (categoryValues.containsKey(label)) {
                if (!interval.equals(categoryValues.get(label))) {
                    throw new IllegalArgumentException("Inconsistent category values.");
                }
                return true;
            }
            if (!allowUnion(index)) {
                return false;
            }
            categoryIndex.put(interval, label);
            categoryValues.put(label, interval);
            return true;
        }

        public boolean unionTime(Instant instant) {
            instantValues.add(instant);
            return true;
        }

        public boolean unionDiscreteTime(Instant instant) {
            instantDiscreteValues.add(instant);
            return true;
        }

        private InnerValues computeInnerValues() {
            TreeSet<Double> tickerValues = new TreeSet<>();
            AtomicReference<Double> min = new AtomicReference<>(Double.NaN);
            AtomicReference<Double> max = new AtomicReference<>(Double.NaN);

            Consumer<Double> collect = (Double x) -> {
                min.set(Double.isNaN(min.get()) ? x : Double.min(min.get(), x));
                max.set(Double.isNaN(max.get()) ? x : Double.max(max.get(), x));
                tickerValues.add(x);
            };

            doubleValues.forEach(collect);
            instantValues.stream().map(i -> (double) i.toEpochMilli()).forEach(collect);
            instantDiscreteValues.stream().map(i -> (double) i.toEpochMilli()).forEach(collect);

            for (CategoryInterval interval : categoryIndex.keySet()) {
                min.set(Double.isNaN(min.get()) ? interval.begin : Double.min(min.get(), interval.begin));
                max.set(Double.isNaN(max.get()) ? interval.end : Double.max(max.get(), interval.end));
                tickerValues.add(interval.value);
            }

            return new InnerValues(min.get(), max.get(), tickerValues.stream().toList());
        }
    }

    private record InnerValues(double min, double max, List<Double> discreteValues) {
    }
}
