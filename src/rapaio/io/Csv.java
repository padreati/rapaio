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

package rapaio.io;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarString;
import rapaio.data.VarType;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.MultiListParam;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.util.IntRule;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serial;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * CSV file reader and writer utility.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Csv extends ParamSet<Csv> {

    @Serial
    private static final long serialVersionUID = -5217533858566320281L;

    /**
     * @return new instance of Csv utility with default parameters values
     */
    public static Csv instance() {
        return new Csv();
    }

    private static final VarType[] DEFAULT_TYPES = new VarType[]{VarType.BINARY, VarType.INT, VarType.LONG, VarType.DOUBLE, VarType.NOMINAL, VarType.STRING};

    private Csv() {
    }

    /**
     * Configures white space trimming for field values. If the white space trimming is enabled,
     * the field values are trimmed at start and end of white char values.
     */
    public final ValueParam<Boolean, Csv> stripSpaces = new ValueParam<>(this, true,
            "trimSpaces", "If true the original value from file is stripped");
    /**
     * Configure the Csv utility to consider a header or not. If the {@code hasHeader} parameter
     * is true the header feature is on, otherwise not.
     */
    public final ValueParam<Boolean, Csv> header = new ValueParam<>(this, true,
            "header", "Specifies if the first row contains column names");
    public final ValueParam<Boolean, Csv> quotes = new ValueParam<>(this, false,
            "quotes", "Specifies if the values are quoted, trimmed at read and added at write");
    /**
     * Configures field character separator.
     */
    public final ValueParam<Character, Csv> separatorChar = new ValueParam<>(this, ',',
            "separatorChar", "Character used to separate row values");
    /**
     * If double quotes (\") character is used to enclose the field values.
     * If this feature is turned on, the values are discarded of eventual first and last characters
     * if those characters are double quotes. This is useful if the separator char is used inside
     * string field values, for example.
     */
    public final ValueParam<Character, Csv> escapeChar = new ValueParam<>(this, '\"',
            "escapeChar", "Escape character");
    public final MultiListParam<VarType, String, Csv> types = new MultiListParam<>(this, new HashMap<>(),
            "types", "Specific type fields which overrides the automatic detection", Objects::nonNull);
    public final ListParam<String, Csv> naValues = new ListParam<>(this, Arrays.asList("?", "", " ", "na", "N/A", "NaN"),
            "naValues", "Values identified as missing value placeholders", (in, out) -> true);
    public final ListParam<VarType, Csv> defaultTypes = new ListParam<>(this, List.of(VarType.BINARY, VarType.DOUBLE, VarType.NOMINAL, VarType.STRING),
            "defaultTypes", "List of default types to be tried at automatic detection", (in, out) -> true);
    /**
     * Specifies the first row number to be collected from csv file. By default this value is 0,
     * which means it will collect starting from the first row. If the value is greater than 0
     * it will skip the first {@code startRow-1} rows.
     */
    public final ValueParam<Integer, Csv> startRow = new ValueParam<>(this, 0,
            "startRow", "Start row of the row intervals to be read");
    /**
     * Specifies the last row number to be collected from csv file. By default this is value
     * is {@code Integer.MAX_VALUE}, which means all rows from file.
     */
    public final ValueParam<Integer, Csv> endRow = new ValueParam<>(this, Integer.MAX_VALUE,
            "endRow", "Last row from row intervals to be read");
    public final ValueParam<IntRule, Csv> skipRows = new ValueParam<>(this, IntRule.all().negate(),
            "skipRows", "Skip rows predicate used to filter rows to be read");
    public final ValueParam<IntRule, Csv> skipCols = new ValueParam<>(this, row -> false,
            "skipCols", "Skip rows predicate used to filter columns to be read");
    public final ValueParam<Frame, Csv> template = new ValueParam<>(this, null,
            "template", "Optional frame templated used to define variable names and type for reading", obj -> true);

    public Frame read(File file) {
        try {
            return read(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException("error at reading file: " + file.getAbsolutePath(), e);
        }
    }

    public Frame readGz(File file) {
        try {
            return read(new GZIPInputStream(new FileInputStream(file)));
        } catch (IOException e) {
            throw new RuntimeException("error at reading file", e);
        }
    }

    public Frame read(String fileName) {
        try {
            return read(new FileInputStream(fileName));
        } catch (IOException e) {
            throw new RuntimeException("error at reading file", e);
        }
    }

    public Frame readUrl(String url) {
        try {
            URL urlObject = new URL(url);
            InputStream is = urlObject.openStream();
            Frame df = read(is);
            is.close();
            return df;
        } catch (IOException e) {
            throw new RuntimeException("cannot read file from url", e);
        }
    }

    public Frame read(Class<?> clazz, String resource) throws IOException {
        InputStream is = clazz.getResourceAsStream(resource);
        if (is == null) {
            throw new IOException("resource: " + resource + " not found in the path of given class: " + clazz.getCanonicalName());
        }
        return read(is);
    }

    public Frame read(InputStream inputStream) throws IOException {
        int rows = 0;
        int allRowsNum = 0;
        List<String> names = new ArrayList<>();
        List<VarSlot> varSlots = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (header.get()) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = parseLine(line);
            }

            while (skipRows.get().test(allRowsNum)) {
                reader.readLine();
                allRowsNum += 1;
            }

            boolean first = true;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                allRowsNum += 1;
                if (skipRows.get().test(allRowsNum - 1)) {
                    continue;
                }


                // build vectors with initial types
                if (first) {
                    List<String> row = parseLine(line);
                    first = false;
                    for (int i = names.size(); i < row.size(); i++) {
                        names.add("V" + (i + 1));
                    }
                    for (String colName : names) {
                        if (template.get() != null) {
                            String[] vn = template.get().varNames();
                            boolean found = false;
                            for (String name : vn) {
                                if (name.equals(colName)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                varSlots.add(new VarSlot(this, template.get().rvar(colName), 0));
                                continue;
                            }
                        }
                        VarType type = types.getReverseKey(colName);
                        if (type != null) {
                            varSlots.add(new VarSlot(this, type, 0));
                        } else {
                            // default type
                            varSlots.add(new VarSlot(this, 0));
                        }
                    }
                }

                if (rows < startRow.get()) {
                    rows++;
                    continue;
                }
                if (rows == endRow.get()) break;
                List<String> row = parseLine(line);
                rows++;
                int len = Math.max(row.size(), names.size());
                for (int i = 0; i < len; i++) {
                    // we have a value in row for which we did not defined a var slot
                    if (i >= varSlots.size()) {
                        names.add("V" + (i + 1));
                        varSlots.add(new VarSlot(this, varSlots.get(0).var.size()));
                        continue;
                    }
                    // we have missing values at the end of the row
                    if (i >= row.size()) {
                        varSlots.get(i).addValue("?");
                        continue;
                    }
                    // gaussian behavior
                    varSlots.get(i).addValue(row.get(i));
                }
            }
        }
        List<Var> variables = new ArrayList<>();
        for (int i = 0; i < varSlots.size(); i++) {
            String name = names.size() > i ? names.get(i) : "V" + (i + 1);
            variables.add(varSlots.get(i).rvar().name(name));
        }
        return SolidFrame.byVars(rows - startRow.get(), variables);
    }

    public List<String> parseLine(String line) {
        List<String> data = new ArrayList<>();
        int start = 0;
        int colNum = 0;
        int end;
        while (start < line.length()) {
            end = start;
            boolean inQuotas = false;
            while (end < line.length()) {
                char ch = line.charAt(end++);
                if (!inQuotas && ch == '"') {
                    inQuotas = true;
                    continue;
                }
                if (inQuotas && ch == escapeChar.get() && end < line.length() && line.charAt(end) == '\"') {
                    end++;
                    continue;
                }
                if (inQuotas && ch == '"') {
                    if (escapeChar.get() == '\"' && end < line.length() && line.charAt(end) == '\"') {
                        end++;
                        continue;
                    }
                    inQuotas = false;
                    continue;
                }
                if (!inQuotas && (ch == separatorChar.get())) {
                    end--;
                    break;
                }
            }

            if (!skipCols.get().test(colNum)) {
                data.add(clean(line.substring(start, end)));
            }

            start = end + 1;
            colNum += 1;
        }
        return data;
    }

    /**
     * Clean the string token. - remove trailing and leading spaces, before and
     * after removing quotes - remove leading and trailing quotes - remove
     * escape quota character
     *
     * @param tok if (trimSpaces) {
     * @return string cleaned
     */
    public String clean(String tok) {
        if (stripSpaces.get()) {
            tok = tok.strip();
        }
        if (quotes.get() && !tok.isEmpty()) {
            if (tok.charAt(0) == '\"') {
                tok = tok.substring(1);
            }
            if (tok.charAt(tok.length() - 1) == '\"') {
                tok = tok.substring(0, tok.length() - 1);
            }
        }
        if (quotes.get()) {
            char[] line = new char[tok.length()];
            int len = 0;
            for (int i = 0; i < tok.length(); i++) {
                if (len < tok.length() - 1 && tok.charAt(i) == escapeChar.get() && tok.charAt(i + 1) == '\"') {
                    line[len++] = '\"';
                    i++;
                    continue;
                }
                line[len++] = tok.charAt(i);
            }
            tok = String.valueOf(line, 0, len);
        }
        if (stripSpaces.get()) {
            tok = tok.strip();
        }
        return tok;
    }

    public void write(Frame df, File file) throws IOException {
        try (OutputStream os = new FileOutputStream(file)) {
            write(df, os);
        }
    }

    public void write(Frame df, String fileName) {
        try {
            try (OutputStream os = new FileOutputStream(fileName)) {
                write(df, os);
            }
        } catch (IOException e) {
            throw new RuntimeException("error at writing file", e);
        }
    }

    public void write(Frame df, OutputStream os) {

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)))) {
            if (header.get()) {
                for (int i = 0; i < df.varNames().length; i++) {
                    if (i != 0) {
                        writer.append(separatorChar.get());
                    }
                    writer.append(df.varNames()[i]);
                }
                writer.append("\n");
            }
            DecimalFormat format = new DecimalFormat("0.###############################");
            for (int i = 0; i < df.rowCount(); i++) {
                for (int j = 0; j < df.varCount(); j++) {
                    if (j != 0) {
                        writer.append(separatorChar.get());
                    }
                    if (df.rvar(j).isMissing(i)) {
                        writer.append("?");
                        continue;
                    }
                    if (df.rvar(j).type().isNominal() || df.rvar(j).type().equals(VarType.STRING)) {
                        writer.append(unclean(df.getLabel(i, j)));
                    } else {
                        writer.append(format.format(df.getDouble(i, j)));
                    }
                }
                writer.append("\n");
            }
            writer.flush();
        }
    }

    public String unclean(String label) {
        char[] line = new char[label.length() * 2];
        int len = 0;
        for (int i = 0; i < label.length(); i++) {
            if (label.charAt(i) == '\"') {
                line[len++] = escapeChar.get();
            }
            line[len++] = label.charAt(i);
        }
        label = String.valueOf(line, 0, len);
        if (quotes.get()) {
            label = "\"" + label + "\"";
        }
        return label;
    }

    static class VarSlot {

        private final Csv parent;

        private final VarType type;
        private Var var;
        private VarString text;

        /**
         * Constructor for slot which does not have a predefined type, it tries the best by using default types
         */
        public VarSlot(Csv parent, int rows) {
            this.parent = parent;
            this.type = null;
            this.var = parent.defaultTypes.get().get(0).newInstance(rows);
            this.text = VarString.empty();
        }

        public VarSlot(Csv parent, VarType varType, int rows) {
            this.parent = parent;
            this.type = varType;
            this.var = varType.newInstance(rows);
            this.text = null;
        }

        public VarSlot(Csv parent, Var template, int rows) {
            this.parent = parent;
            this.type = template.type();
            this.var = template.newInstance(rows);
            this.text = null;
        }

        public void addValue(String value) {
            if (parent.naValues.get().contains(value)) {
                value = "?";
            }
            if (type == null) {

                // for default values

                while (true) {

                    // try first to add value to the current default type
                    try {
                        var.addLabel(value);
                        if (text != null) {
                            text.addLabel(value);
                        }
                        return;
                    } catch (IllegalArgumentException th) {
                        // if it's the last default type, than nothing else could be done
                        if (var.type() == parent.defaultTypes.get().get(parent.defaultTypes.get().size() - 1)) {
                            throw new IllegalArgumentException(
                                    String.format("Could not parse value %s in type %s. Error: %s",
                                            value, var.type(), th.getMessage()));
                        }
                    }

                    // have to find an upgrade
                    // find current default type position
                    int pos = 0;
                    for (int i = 0; i < parent.defaultTypes.get().size(); i++) {
                        if (!parent.defaultTypes.get().get(i).equals(var.type())) continue;
                        pos = i + 1;
                        break;
                    }

                    // try successive default type upgrades, if the last available fails also than throw an exception
                    for (int i = pos; i < parent.defaultTypes.get().size(); i++) {
                        try {
                            var = parent.defaultTypes.get().get(i).newInstance();
                            if (text != null && text.size() > 0)
                                text.stream().forEach(s -> var.addLabel(s.getLabel()));
                            if (i == parent.defaultTypes.get().size() - 1)
                                text = null;
                            break;
                        } catch (Exception th) {
                            if (i == parent.defaultTypes.get().size() - 1) {
                                throw new IllegalArgumentException(
                                        String.format("Could not parse value %s in type %s. Error: %s",
                                                value, var.type(), th.getMessage()));
                            }
                        }
                    }
                }
            } else {

                // for non-default values

                try {
                    var.addLabel(value);
                } catch (IllegalArgumentException th) {
                    throw new IllegalArgumentException(
                            String.format("Could not parse value %s in type %s for variable with name: %s. Error: %s",
                                    value, var.type(), var.name(), th.getMessage()));
                }
            }
        }

        public Var rvar() {
            return var;
        }
    }
}

