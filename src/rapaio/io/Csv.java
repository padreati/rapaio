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
 */

package rapaio.io;

import rapaio.data.*;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Comma separated file reader and writer utility.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Csv {

    private boolean trimSpaces = true;
    private boolean header = true;
    private boolean quotas = true;
    private char separatorChar = ',';
    private char escapeChar = '\"';
    private HashMap<String, VarType> typeFieldHints = new HashMap<>();
    private HashSet<String> naValues = new HashSet<>();
    private VarType[] defaultTypes = new VarType[]{VarType.BINARY, VarType.INDEX, VarType.NUMERIC, VarType.NOMINAL};
    private int startRow = 0;
    private int endRow = Integer.MAX_VALUE;
    private HashSet<Integer> skipRows = new HashSet<>();
    private HashSet<Integer> skipCols = new HashSet<>();

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

    public Csv withQuotas(boolean quotas) {
        this.quotas = quotas;
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

    public Csv skipRows(int... rows) {
        if (rows != null && rows.length > 0) {
            for (int row : rows) {
                skipRows.add(row);
            }
        }

        return this;
    }

    public Csv skipCols(int... cols) {
        if (cols != null && cols.length > 0) {
            for (int col : cols) {
                skipCols.add(col);
            }
        }

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
            while (skipRows.contains(allRowsNum)) {
                reader.readLine();
                allRowsNum += 1;
            }

            if (header) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = parseLine(line);
            }

            boolean first = true;
            while (true) {
                String line = reader.readLine();
                allRowsNum += 1;

                if (skipRows.contains(allRowsNum)) {
                    continue;
                }

                if (line == null) {
                    break;
                }
                List<String> row = parseLine(line);

                // build vectors with initial types
                if (first) {
                    first = false;
                    for (int i = names.size(); i < row.size(); i++) {
                        names.add("V" + (i + 1));
                    }
                    for (String colName : names) {
                        if (typeFieldHints.containsKey(colName)) {
                            varSlots.add(new VarSlot(this, typeFieldHints.get(colName)));
                        } else {
                            // default getType
                            varSlots.add(new VarSlot(this));
                        }
                    }
                }

                if (rows < startRow) {
                    rows++;
                    continue;
                }
                if (rows == endRow) break;
                rows++;
                int len = Math.max(row.size(), names.size());
                for (int i = 0; i < len; i++) {
                    // we have a value in row for which we did not defined a var slot
                    if (i >= varSlots.size()) {
                        varSlots.add(new VarSlot(this));
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
            variables.add(varSlots.get(i).getVar().withName(names.get(i)));
        }
        return SolidFrame.newWrapOf(rows - startRow, variables);
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

            if (!skipCols.contains(colNum)) {
                data.add(clean(line.substring(start, end)));
            }

            start = end + 1;
            colNum += 1;
        }
        return data;
    }

    /**
     * Clean the string token. - remove trailing and leading spaces, before and
     * after removing quotas - remove leading and trailing quotas - remove
     * escape quota character
     *
     * @param tok if (trimSpaces) {
     * @return
     */
    private String clean(String tok) {
        if (trimSpaces) {
            tok = tok.trim();
        }
        if (quotas && !tok.isEmpty()) {
            if (tok.charAt(0) == '\"') {
                tok = tok.substring(1);
            }
            if (tok.charAt(tok.length() - 1) == '\"') {
                tok = tok.substring(0, tok.length() - 1);
            }
        }
        if (quotas) {
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
        if (quotas) {
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
        public VarSlot(Csv parent) {
            this.parent = parent;
            this.type = null;
            this.var = parent.defaultTypes[0].newInstance();
            this.text = Text.newEmpty();
        }

        public VarSlot(Csv parent, VarType varType) {
            this.parent = parent;
            this.type = varType;
            this.var = varType.newInstance();
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
                            String.format("Could not parse value %s in type %s. Error: %s",
                                    value, var.type(), th.getMessage()));
                }
            }
        }

        public Var getVar() {
            return var;
        }
    }
}

