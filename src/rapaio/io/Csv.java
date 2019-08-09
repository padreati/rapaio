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

package rapaio.io;

import rapaio.data.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import static java.util.stream.Collectors.toSet;

/**
 * CSV file reader and writer utility.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Csv {

    /**
     * @return new instance of Csv utility with default parameters values
     */
    public static Csv instance() {
        return new Csv();
    }

    private static VType[] DEFAULT_TYPES = new VType[]{VType.BINARY, VType.INT, VType.LONG, VType.DOUBLE, VType.NOMINAL, VType.TEXT};

    private boolean trimSpaces = true;
    private boolean header = true;
    private boolean quotes = false;
    private char separatorChar = ',';
    private char escapeChar = '\"';
    private HashMap<String, VType> typeFieldHints = new HashMap<>();
    private HashSet<String> naValues = new HashSet<>(Arrays.asList("?", "", " ", "na", "N/A", "NaN"));
    private VType[] defaultTypes = new VType[]{VType.BINARY, VType.DOUBLE, VType.NOMINAL, VType.TEXT};
    private int startRow = 0;
    private int endRow = Integer.MAX_VALUE;
    private Predicate<Integer> skipRows = row -> false;
    private Predicate<Integer> skipCols = row -> false;
    private Frame template;

    private Csv() {
    }

    /**
     * Configure the Csv utility to consider a header or not. If the {@code hasHeader} parameter
     * is true the header feature is on, otherwise not.
     *
     * @param hasHeader if true the header feature is on, otherwise not
     * @return the Csv utility instance
     */
    public Csv withHeader(boolean hasHeader) {
        this.header = hasHeader;
        return this;
    }

    /**
     * @return true if header feature is on, otherwise it returns false
     */
    public boolean hashHeader() {
        return header;
    }

    /**
     * Configures field character separator.
     *
     * @param separatorChar the field char separator value
     * @return Csv utility instance
     */
    public Csv withSeparatorChar(char separatorChar) {
        this.separatorChar = separatorChar;
        return this;
    }

    /**
     * @return char used as field separator
     */
    public char getSeparatorChar() {
        return separatorChar;
    }

    /**
     * If double quotes (\") character is used to enclose the field values.
     * If this feature is turned on, the values are discarded of eventual first and last characters
     * if those characters are double quotes. This is useful if the separator char is used inside
     * string field values, for example.
     *
     * @param quotes true if feature is turned on, false otherwise
     * @return Csv utility instance
     */
    public Csv withQuotes(boolean quotes) {
        this.quotes = quotes;
        return this;
    }

    public boolean hasQuotes() {
        return quotes;
    }

    /**
     * Configures default escape char. If the field value contains escape char, than the following char after
     * the escape does not have semantics and is considered as part of the field value. This enables one to
     * use inside field values chars with semantics, like separator char.
     *
     * @param escapeChar configured escape char
     * @return Csv instance utility
     */
    public Csv withEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    /**
     * Configures white space trimming for field values. If the white space trimming is enabled,
     * the field values are trimmed at start and end of white char values.
     *
     * @param trimSpaces if true feature is enabled, false otherwise
     * @return Csv instance utility
     */
    public Csv withTrimSpaces(boolean trimSpaces) {
        this.trimSpaces = trimSpaces;
        return this;
    }

    /**
     * Specifies the first row number to be collected from csv file. By default this value is 0,
     * which means it will collect starting from the first row. If the value is greater than 0
     * it will skip the first {@code startRow-1} rows.
     *
     * @param startRow first row to be collected
     * @return Csv instance utility
     */
    public Csv withStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    /**
     * @return first row number to be collected
     */
    public int getStartRow() {
        return startRow;
    }

    /**
     * Specifies the last row number to be collected from csv file. By default this is value
     * is {@code Integer.MAX_VALUE}, which means all rows from file.
     *
     * @param endRow last row to be collected
     * @return Csv instance utility
     */
    public Csv withEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public Csv withRows(int... rows) {
        final Set<Integer> skip = Arrays.stream(rows).boxed().collect(toSet());
        skipRows = row -> !skip.contains(row);
        return this;
    }

    public Csv withRows(Predicate<Integer> p) {
        skipRows = p.negate();
        return this;
    }

    public Csv withSkipRows(int... rows) {
        final Set<Integer> skip = Arrays.stream(rows).boxed().collect(toSet());
        skipRows = skip::contains;
        return this;
    }

    public Csv withSkipRows(Predicate<Integer> p) {
        skipRows = p;
        return this;
    }

    public Csv withCols(int... cols) {
        final Set<Integer> skip = Arrays.stream(cols).boxed().collect(toSet());
        skipCols = row -> !skip.contains(row);
        return this;
    }

    public Csv withCols(Predicate<Integer> p) {
        skipCols = p.negate();
        return this;
    }

    public Csv withSkipCols(int... cols) {
        Set<Integer> skip = Arrays.stream(cols).boxed().collect(toSet());
        skipCols = skip::contains;
        return this;
    }

    public Csv withSkipCols(Predicate<Integer> p) {
        skipCols = p;
        return this;
    }

    public Csv withTypes(VType vType, String... fields) {
        Arrays.stream(fields).forEach(field -> typeFieldHints.put(field, vType));
        return this;
    }

    public Csv withDefaultTypes(VType... defaultTypes) {
        this.defaultTypes = defaultTypes;
        return this;
    }

    public Csv withDefaultTypes() {
        this.defaultTypes = DEFAULT_TYPES;
        return this;
    }

    public Csv withNAValues(String... values) {
        this.naValues = new HashSet<>();
        Collections.addAll(naValues, values);
        return this;
    }

    public Csv withTemplate(Frame template) {
        this.template = template;
        return this;
    }

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

            if (header) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = parseLine(line);
            }

            while (skipRows.test(allRowsNum)) {
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
                if (skipRows.test(allRowsNum - 1)) {
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
                        if (template != null) {
                            String[] vn = template.varNames();
                            boolean found = false;
                            for (String name : vn) {
                                if (name.equals(colName)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (found) {
                                varSlots.add(new VarSlot(this, template.rvar(colName), 0));
                                continue;
                            }
                        }
                        if (typeFieldHints.containsKey(colName)) {
                            varSlots.add(new VarSlot(this, typeFieldHints.get(colName), 0));
                        } else {
                            // default type
                            varSlots.add(new VarSlot(this, 0));
                        }
                    }
                }

                if (rows < startRow) {
                    rows++;
                    continue;
                }
                if (rows == endRow) break;
                List<String> row = parseLine(line);
                rows++;
                int len = Math.max(row.size(), names.size());
                for (int i = 0; i < len; i++) {
                    // we have a value in row for which we did not defined a var slot
                    if (i >= varSlots.size()) {
                        names.add("V" + (i + 1));
                        varSlots.add(new VarSlot(this, varSlots.get(0).var.rowCount()));
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
            variables.add(varSlots.get(i).rvar().withName(name));
        }
        return SolidFrame.byVars(rows - startRow, variables);
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
                if (inQuotas && ch == escapeChar) {
                    if (end < line.length() && line.charAt(end) == '\"') {
                        end++;
                        continue;
                    }
                }
                if (inQuotas && ch == '"') {
                    if (escapeChar == '\"') {
                        if (end < line.length() && line.charAt(end) == '\"') {
                            end++;
                            continue;
                        }
                    }
                    inQuotas = false;
                    continue;
                }
                if (!inQuotas && (ch == separatorChar)) {
                    end--;
                    break;
                }
            }

            if (!skipCols.test(colNum)) {
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
        if (trimSpaces) {
            tok = tok.trim();
        }
        if (quotes && !tok.isEmpty()) {
            if (tok.charAt(0) == '\"') {
                tok = tok.substring(1);
            }
            if (tok.charAt(tok.length() - 1) == '\"') {
                tok = tok.substring(0, tok.length() - 1);
            }
        }
        if (quotes) {
            char[] line = new char[tok.length()];
            int len = 0;
            for (int i = 0; i < tok.length(); i++) {
                if (len < tok.length() - 1 && tok.charAt(i) == escapeChar && tok.charAt(i + 1) == '\"') {
                    line[len++] = '\"';
                    i++;
                    continue;
                }
                line[len++] = tok.charAt(i);
            }
            tok = String.valueOf(line, 0, len);
        }
        if (trimSpaces) {
            tok = tok.trim();
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
            if (header) {
                for (int i = 0; i < df.varNames().length; i++) {
                    if (i != 0) {
                        writer.append(separatorChar);
                    }
                    writer.append(df.varNames()[i]);
                }
                writer.append("\n");
            }
            DecimalFormat format = new DecimalFormat("0.###############################");
            for (int i = 0; i < df.rowCount(); i++) {
                for (int j = 0; j < df.varCount(); j++) {
                    if (j != 0) {
                        writer.append(separatorChar);
                    }
                    if (df.rvar(j).isMissing(i)) {
                        writer.append("?");
                        continue;
                    }
                    if (df.rvar(j).type().isNominal() || df.rvar(j).type().equals(VType.TEXT)) {
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
                line[len++] = escapeChar;
            }
            line[len++] = label.charAt(i);
        }
        label = String.valueOf(line, 0, len);
        if (quotes) {
            label = "\"" + label + "\"";
        }
        return label;
    }

    static class VarSlot {

        private final Csv parent;

        private final VType type;
        private Var var;
        private VarText text;

        /**
         * Constructor for slot which does not have a predefined type, it tries the best by using default types
         */
        public VarSlot(Csv parent, int rows) {
            this.parent = parent;
            this.type = null;
            this.var = parent.defaultTypes[0].newInstance(rows);
            this.text = VarText.empty();
        }

        public VarSlot(Csv parent, VType vType, int rows) {
            this.parent = parent;
            this.type = vType;
            this.var = vType.newInstance(rows);
            this.text = null;
        }

        public VarSlot(Csv parent, Var template, int rows) {
            this.parent = parent;
            this.type = template.type();
            this.var = template.newInstance(rows);
            this.text = null;
        }

        public void addValue(String value) {
            if (parent.naValues.contains(value)) {
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
                    } catch (Throwable th) {
                        // if it's the last default type, than nothing else could be done
                        if (var.type() == parent.defaultTypes[parent.defaultTypes.length - 1]) {
                            throw new IllegalArgumentException(
                                    String.format("Could not parse value %s in type %s. Error: %s",
                                            value, var.type(), th.getMessage()));
                        }
                    }

                    // have to find an upgrade
                    // find current default type position
                    int pos = 0;
                    for (int i = 0; i < parent.defaultTypes.length; i++) {
                        if (!parent.defaultTypes[i].equals(var.type())) continue;
                        pos = i + 1;
                        break;
                    }

                    // try successive default type upgrades, if the last available fails also than throw an exception
                    for (int i = pos; i < parent.defaultTypes.length; i++) {
                        try {
                            var = parent.defaultTypes[i].newInstance();
                            if (text != null && text.rowCount() > 0)
                                text.stream().forEach(s -> var.addLabel(s.getLabel()));
                            if (i == parent.defaultTypes.length - 1)
                                text = null;
                            break;
                        } catch (Exception th) {
                            if (i == parent.defaultTypes.length - 1) {
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
                } catch (Throwable th) {
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

