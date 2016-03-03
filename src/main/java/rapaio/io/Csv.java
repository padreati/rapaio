/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.util.func.SPredicate;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.zip.GZIPInputStream;

import static java.util.stream.Collectors.toSet;

/**
 * Comma separated file reader and writer utility.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Csv {

    private boolean trimSpaces = true;
    private boolean header = true;
    private boolean quotes = false;
    private char separatorChar = ',';
    private char escapeChar = '\"';
    private HashMap<String, VarType> typeFieldHints = new HashMap<>();
    private HashSet<String> naValues = new HashSet<>();
    private VarType[] defaultTypes = new VarType[]{VarType.BINARY, VarType.INDEX, VarType.NUMERIC, VarType.NOMINAL};
    private int startRow = 0;
    private int endRow = Integer.MAX_VALUE;
    private Predicate<Integer> skipRows = row -> false;
    private Predicate<Integer> skipCols = row -> false;
    private Frame template;

    public Csv() {
        naValues.add("?");
    }

    public Csv withHeader(boolean hasHeader) {
        this.header = hasHeader;
        return this;
    }

    public Csv withSeparatorChar(char separator) {
        this.separatorChar = separator;
        return this;
    }

    public Csv withQuotes(boolean quotes) {
        this.quotes = quotes;
        return this;
    }

    public Csv withEscapeChar(char escapeChar) {
        this.escapeChar = escapeChar;
        return this;
    }

    public Csv withTrimSpaces(boolean trimSpaces) {
        this.trimSpaces = trimSpaces;
        return this;
    }

    public Csv withStartRow(int startRow) {
        this.startRow = startRow;
        return this;
    }

    public Csv withEndRow(int endRow) {
        this.endRow = endRow;
        return this;
    }

    public Csv withSkipRows(int... rows) {
        final Set<Integer> skip = Arrays.stream(rows).boxed().collect(toSet());
        skipRows = skip::contains;
        return this;
    }

    public Csv withRows(int... rows) {
        final Set<Integer> skip = Arrays.stream(rows).boxed().collect(toSet());
        skipRows = row -> !skip.contains(row);
        return this;
    }

    public Csv withSkipRows(Predicate<Integer> p) {
        skipRows = p;
        return this;
    }

    public Csv withSkipCols(int... cols) {
        final Set<Integer> skip = Arrays.stream(cols).boxed().collect(toSet());
        Predicate<Integer> old = skipCols;
        skipCols = row -> old.test(row) || skip.contains(row);
        return this;
    }

    public Csv withSkipCols(SPredicate<Integer> p) {
        skipCols = p;
        return this;
    }

    public Csv withTypes(VarType varType, String... fields) {
        Arrays.stream(fields).forEach(field -> typeFieldHints.put(field, varType));
        return this;
    }

    public Csv withDefaultTypes(VarType... defaultTypes) {
        this.defaultTypes = defaultTypes;
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

    public Frame read(File file) throws IOException {
        return read(new FileInputStream(file));
    }

    public Frame readGz(File file) throws IOException {
        return read(new GZIPInputStream(new FileInputStream(file)));
    }

    public Frame read(String fileName) throws IOException {
        return read(new FileInputStream(fileName));
    }

    public Frame read(Class<?> clazz, String resource) throws IOException {
        return read(clazz.getResourceAsStream(resource));
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
                                varSlots.add(new VarSlot(this, template.var(colName), 0));
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
                        names.add("V" + (i+1));
                        varSlots.add(new VarSlot(this, varSlots.get(0).var.rowCount()));
                        continue;
                    }
                    // we have missing values at the end of the row
                    if (i >= row.size()) {
                        varSlots.get(i).addValue("?");
                        continue;
                    }
                    // normal behavior
                    varSlots.get(i).addValue(row.get(i));
                }
            }
        }
        List<Var> variables = new ArrayList<>();
        for (int i = 0; i < varSlots.size(); i++) {
            String name = names.size() > i ? names.get(i) : "V" + (i + 1);
            variables.add(varSlots.get(i).var().withName(name));
        }
        return SolidFrame.wrapOf(rows - startRow, variables);
    }

    List<String> parseLine(String line) {
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
    private String clean(String tok) {
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

    public void write(Frame df, String fileName) throws IOException {
        try (OutputStream os = new FileOutputStream(fileName)) {
            write(df, os);
        }
    }

    public void write(Frame df, OutputStream os) throws IOException {

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
                    if (df.var(j).missing(i)) {
                        writer.append("?");
                        continue;
                    }
                    if (df.var(j).type().isNominal() || df.var(j).type().equals(VarType.TEXT)) {
                        writer.append(unclean(df.label(i, j)));
                    } else {
                        writer.append(format.format(df.value(i, j)));
                    }
                }
                writer.append("\n");
            }
            writer.flush();
        }
    }

    private String unclean(String label) {
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

        private final VarType type;
        private Var var;
        private Text text;

        /**
         * Constructor for slot which does not have a predefined type, it tries the best by using default types
         */
        public VarSlot(Csv parent, int rows) {
            this.parent = parent;
            this.type = null;
            this.var = parent.defaultTypes[0].newInstance(rows);
            this.text = Text.empty();
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
                                text.stream().forEach(s -> var.addLabel(s.label()));
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

        public Var var() {
            return var;
        }
    }
}

