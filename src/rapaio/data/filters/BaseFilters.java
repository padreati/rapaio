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

package rapaio.data.filters;

import rapaio.core.ColRange;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Quantiles;
import rapaio.data.*;
import rapaio.data.Var;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.MappedVar;
import rapaio.data.mapping.Mapping;
import rapaio.data.stream.FSpot;
import rapaio.data.stream.VSpot;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides filters for frames.
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class BaseFilters implements Serializable {

    private BaseFilters() {
    }


    /**
     * Frame filters
     */
    //=================================================================================

    /**
     * Convert to numeric values all the columns which are nominal.
     * <p>
     * All the other columns remain the same.
     *
     * @param df input frame
     * @return frame with value converted columns
     */
    public static Frame toNumeric(Frame df) {
        Var[] vars = new Var[df.colCount()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = toNumeric(df.col(i));
        }
        return new SolidFrame(df.rowCount(), vars, df.colNames(), df.weights());
    }

    /**
     * Remove columns specified in a column range from a frame.
     *
     * @param df       frame
     * @param colRange column range
     * @return original frame without columns specified in {
     */
    public static Frame removeCols(Frame df, String colRange) {
        ColRange range = new ColRange(colRange);
        Set<Integer> indexes = new HashSet<>(range.parseColumnIndexes(df));
        List<String> names = new ArrayList<>();
        for (int i = 0; i < df.colCount(); i++) {
            if (indexes.contains(i)) continue;
            names.add(df.colNames()[i]);
        }
        return new MappedFrame(df, df.mapping(), names);
    }

    /**
     * Remove columns from a frame by specifying which columns to keep.
     *
     * @param df       frame
     * @param colRange column range
     * @return original frame which has only columns specified in {
     */
    public static Frame retainCols(Frame df, String colRange) {
        ColRange range = new ColRange(colRange);
        final List<Integer> indexes = range.parseColumnIndexes(df);
        String[] names = new String[indexes.size()];
        int posIndexes = 0;
        for (int i = 0; i < df.colCount(); i++) {
            if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
                names[posIndexes] = df.colNames()[i];
                posIndexes++;
            }
        }
        return new MappedFrame(df, df.mapping(), Arrays.asList(names));
    }

    /**
     * Retain only numeric columns from a frame.
     */
    public static Frame retainNumeric(Frame df) {
        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < df.colCount(); i++) {
            if (df.col(i).type().isNumeric()) {
                vars.add(df.col(i));
                names.add(df.colNames()[i]);
            }
        }
        return new SolidFrame(df.rowCount(), vars, names, df.weights());
    }

    /**
     * Retain only nominal columns from a frame.
     */
    public static Frame retainNominal(Frame df) {
        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < df.colCount(); i++) {
            if (df.col(i).type().isNominal()) {
                vars.add(df.col(i));
                names.add(df.colNames()[i]);
            }
        }
        return new SolidFrame(df.rowCount(), vars, names, df.weights());
    }

    public static Frame discretize(Frame df, ColRange colRange, int bins, boolean useQuantiles) {
        if (df.isMappedFrame()) {
            throw new IllegalArgumentException("Not allowed for mapped frame");
        }
        if (df.rowCount() < bins) {
            throw new IllegalArgumentException("Number of bins greater than number of getRowCount");
        }
        Set<Integer> colSet = new HashSet<>(colRange.parseColumnIndexes(df));
        for (int col : colSet) {
            if (!df.col(col).type().isNumeric()) {
                throw new IllegalArgumentException("Non-numeric column found in column range");
            }
        }
        Set<String> dict = new HashSet<>();
        for (int i = 0; i < bins; i++) {
            dict.add(String.valueOf(i + 1));
        }
        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (int i = 0; i < df.colCount(); i++) {
            if (!colSet.contains(i)) {
                vars.add(df.col(i));
                continue;
            }
            Var origin = df.col(i);
            Var discrete = new Nominal(origin.rowCount(), dict);
            if (!useQuantiles) {
                Var sorted = sort(df.col(i));
                int width = (int) Math.ceil(df.rowCount() / (1. * bins));
                for (int j = 0; j < bins; j++) {
                    for (int k = 0; k < width; k++) {
                        if (j * width + k >= df.rowCount())
                            break;
                        if (sorted.missing(j * width + k))
                            continue;
                        int rowId = sorted.rowId(j * width + k);
                        discrete.setLabel(rowId, String.valueOf(j + 1));
                    }
                }
            } else {
                double[] p = new double[bins];
                for (int j = 0; j < p.length; j++) {
                    p[j] = j / (1. * bins);
                }
                double[] q = new Quantiles(origin, p).getValues();
                for (int j = 0; j < origin.rowCount(); j++) {
                    if (origin.missing(j))
                        continue;
                    double value = origin.value(j);
                    int index = Arrays.binarySearch(q, value);
                    if (index < 0) {
                        index = -index - 1;
                    } else {
                        index++;
                    }
                    discrete.setLabel(j, String.valueOf(index));
                }
            }
            vars.add(discrete);
            names.add(df.colNames()[i]);
        }
        return new SolidFrame(df.rowCount(), vars, names, df.weights());
    }

    /**
     * Consolidates all the common nominal columns from all frames given as parameter.
     *
     * @param source list of source frames
     * @return list of frames with common nominal columns consolidated
     */
    public static List<Frame> consolidate(List<Frame> source) {

        // learn reunion of labels for all columns
        HashMap<String, HashSet<String>> dicts = new HashMap<>();
        for (int i = 0; i < source.size(); i++) {
            for (Frame frame : source) {
                for (String colName : frame.colNames()) {
                    if (!frame.col(colName).type().isNominal()) {
                        continue;
                    }
                    if (!dicts.containsKey(colName)) {
                        dicts.put(colName, new HashSet<>());
                    }
                    dicts.get(colName).addAll(Arrays.asList(frame.col(colName).dictionary()));
                }
            }
        }

        // rebuild each frame according with the new consolidated data
        List<Frame> dest = new ArrayList<>();
        for (Frame frame : source) {
            Var[] vars = new Var[frame.colCount()];
            for (int i = 0; i < frame.colCount(); i++) {
                Var v = frame.col(i);
                String colName = frame.colNames()[i];
                if (!v.type().isNominal()) {
                    vars[i] = v;
                } else {
                    vars[i] = new Nominal(v.rowCount(), dicts.get(colName));
                    for (int k = 0; k < vars[i].rowCount(); k++) {
                        vars[i].setLabel(k, v.label(k));
                    }
                }
            }
            dest.add(new SolidFrame(frame.rowCount(), vars, frame.colNames(), frame.weights()));
        }

        return dest;
    }

    /**
     * Split a frame into multiple frames, one for each label in
     * the term dictionary of the nominal value given as parameter.
     *
     * @param df      source frame
     * @param colName index fo the nominal column
     * @return a map of frames, with nominal labels as keys
     */
    public static Map<String, Frame> groupByNominal(final Frame df, final String colName) {
        if (!df.col(colName).type().isNominal()) {
            throw new IllegalArgumentException("Index does not specify a nominal attribute");
        }
        Map<String, Frame> frames = new HashMap<>();
        String[] dict = df.col(colName).dictionary();
        final Mapping[] mappings = new Mapping[dict.length];
        for (int i = 0; i < dict.length; i++) {
            mappings[i] = new Mapping();
        }

        df.stream().forEach((FSpot fi) -> {
            int index = fi.index(colName);
            mappings[index].add(fi.rowId());
        });
        for (int i = 0; i < mappings.length; i++) {
            frames.put(dict[i], new MappedFrame(df, mappings[i]));
        }
        return frames;
    }

    /**
     * Shuffle the order of getRowCount from specified frame.
     *
     * @param df source frame
     * @return shuffled frame
     */
    public static Frame shuffle(Frame df) {
        ArrayList<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < df.rowCount(); i++) {
            mapping.add(df.rowId(i));
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(RandomSource.nextInt(i), mapping.get(i - 1)));
        }
        return new MappedFrame(df, new Mapping(mapping));
    }

    public static Frame sort(Frame df, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < df.rowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        List<Integer> ids = new ArrayList<>();
        for (Integer aMapping : mapping) {
            ids.add(df.rowId(aMapping));
        }
        return new MappedFrame(df, new Mapping(ids));
    }


    public static Frame delta(Frame source, Frame remove) {
        Set<Integer> existing = remove.stream().map(FSpot::rowId).collect(Collectors.toSet());
        return source.stream().filter(s -> !existing.contains(s.rowId())).toMappedFrame();
    }

    public static List<Frame> combine(String name, List<Frame> frames, String... combined) {
        Set<String> dict = new HashSet<>();
        dict.add("");
        for (Frame frame1 : frames) {
            if (frame1.isMappedFrame()) {
                throw new IllegalArgumentException("Not allowed mapped frames");
            }
        }

        for (String aCombined : combined) {
            String[] vdict = frames.get(0).col(aCombined).dictionary();
            Set<String> newdict = new HashSet<>();
            for (String term : dict) {
                for (String aVdict : vdict) {
                    newdict.add(term + "." + aVdict);
                }
            }
            dict = newdict;
        }

        List<Frame> result = new ArrayList<>();
        for (Frame frame : frames) {
            List<Var> vars = new ArrayList<>();
            for (int j = 0; j < frame.colCount(); j++) {
                vars.add(frame.col(j));
            }
            Var col = new Nominal(frame.rowCount(), dict);
            for (int j = 0; j < frame.rowCount(); j++) {
                StringBuilder sb = new StringBuilder();
                for (int k = 0; k < combined.length; k++) {
                    sb.append(".").append(frame.label(j, frame.colIndex(combined[k])));
                }
                col.setLabel(j, sb.toString());
            }
            vars.add(col);
            result.add(new SolidFrame(frame.rowCount(), vars, frame.colNames(), frame.weights()));
        }
        return result;

    }

    public static Var completeCases(Var source) {
        return source.stream().filter(s -> !s.missing()).toMappedVar();
    }

    /**
     * Returns a mapped frame with cases which does not contain missing values
     * in any column of the frame.
     *
     * @param source source frame
     * @return mapped frame with complete cases
     */
    public static Frame completeCases(Frame source) {
        return completeCases(source, new ColRange("all"));
    }

    public static Frame completeCases(Frame source, ColRange colRange) {
        List<Integer> selectedCols = colRange.parseColumnIndexes(source);
        return source.stream().filter(s -> !selectedCols.stream().anyMatch(s::missing)).toMappedFrame();
    }

    /**
     * Vector filters
     */
    //=================================================================================

    /**
     * Convert a var to numeric by parsing as numbers the nominal
     * labels, or promoting to double the numeric values.
     * <p>
     * If the input value is already a numeric var, the input var is
     * returned untouched.
     *
     * @param v input var
     * @return converted value var
     */
    public static Numeric toNumeric(Var v) {
        if (v.type().equals(VarType.NUMERIC)) {
            return (Numeric) v;
        }
        final Numeric result = new Numeric();
        v.stream().forEach((VSpot vi) -> {
            if (vi.missing()) {
                result.addMissing();
            } else {
                switch (v.type()) {
                    case NOMINAL:
                        try {
                            double value = Double.parseDouble(vi.label());
                            result.addValue(value);
                        } catch (NumberFormatException nfe) {
                            result.addMissing();
                        }
                        break;
                    case INDEX:
                        result.addValue(vi.index());
                        break;
                }
            }
        });
        return result;
    }

    /**
     * Converts a given var to index, either by parsing nominal labels,
     * either by rounding the numeric values.
     * Any error produces a missing value.
     *
     * @param v input var
     * @return resulted index var
     */
    public static Index toIndex(Var v) {
        if (v.type().equals(VarType.INDEX)) {
            return (Index) v;
        }
        final Index result = new Index();
        v.stream().forEach((VSpot inst) -> {
            if (inst.missing()) {
                result.addMissing();
            } else {
                switch (v.type()) {
                    case NUMERIC:
                        result.addIndex((int) Math.rint(inst.value()));
                        break;
                    case NOMINAL:
                        int value = Integer.parseInt(inst.label());
                        result.addIndex(value);
                        break;
                }
            }
        });
        return result;
    }

    /**
     * Alter valid numeric values with normally distributed noise.
     * <p>
     * Noise comes from a normal distribution with mean 0 and standard deviation
     * 0.1
     *
     * @param var input values
     * @return altered values
     */

    public static Numeric jitter(Var var) {
        return jitter(var, 0.1);
    }

    /**
     * Alter valid numeric values with normally distributed noise.
     * <p>
     * Noise comes from a normal distribution with mean 0 and standard deviation
     * specified by {
     *
     * @param var input values
     * @param sd  standard deviation of the normally distributed noise
     * @return altered values
     */
    public static Numeric jitter(Var var, double sd) {
        Normal normal = new Normal(0, sd);
        Numeric result = new Numeric(var.rowCount());
        result.stream().forEach(s -> result.setValue(s.row(),
                var.value(s.row()) + normal.quantile(RandomSource.nextDouble())));
        return result;
    }

    /**
     * Set missing values for all nominal values included
     * in missing values array {@param missingValues}.
     *
     * @param var           source var
     * @param missingValues labels for missing values
     * @return original var with missing value on matched positions
     */
    public static Var fillMissingValues(Var var, Collection<String> missingValues) {
        if (!var.type().isNominal()) {
            throw new IllegalArgumentException("Vector is not nominal.");
        }
        var.stream().forEach((VSpot inst) -> {
            if (missingValues.contains(inst.label()))
                inst.setMissing();
        });
        return var;
    }


    /**
     * Builds a mapped var with shuffled rowIds
     *
     * @param v source var
     * @return shuffled var
     */
    public static Var shuffle(Var v) {
        ArrayList<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < v.rowCount(); i++) {
            mapping.add(v.rowId(i));
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(RandomSource.nextInt(i), mapping.get(i - 1)));
        }
        return new MappedVar(v.source(), new Mapping(mapping));
    }

    public static Var sort(Var v) {
        return sort(v, true);
    }

    public static Var sort(Var v, boolean asc) {
        if (v.type().isNumeric()) {
            return sort(v, RowComparators.numericComparator(v, asc));
        }
        return sort(v, RowComparators.nominalComparator(v, asc));
    }

    @SafeVarargs
    public static Var sort(Var var, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < var.rowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        List<Integer> ids = new ArrayList<>();
        for (Integer aMapping : mapping) {
            ids.add(var.rowId(aMapping));
        }
        return new MappedVar(var.source(), new Mapping(ids));
    }
}
