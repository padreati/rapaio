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

package rapaio.io;

import rapaio.data.*;
import rapaio.data.Var;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Csv {

    private boolean trimSpaces = true;
    private boolean header = true;
    private boolean quotas = true;
    private char separatorChar = ',';
    private char escapeChar = '\"';
    private HashSet<String> numericFieldHints = new HashSet<>();
    private HashSet<String> indexFieldHints = new HashSet<>();
    private HashSet<String> nominalFieldHints = new HashSet<>();
    private HashSet<String> naValues = new HashSet<>();
    private VarType defaultType = VarType.NOMINAL;
    private int startRow = 0;
    private int endRow = Integer.MAX_VALUE;

    public Csv() {
        naValues.add("?");
//        naValues.add("N/A");
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

    public Csv withNumericFields(String... fields) {
        numericFieldHints = new HashSet<>(Arrays.asList(fields));
        return this;
    }

    public Csv withIndexFields(String... fields) {
        indexFieldHints = new HashSet<>(Arrays.asList(fields));
        return this;
    }

    public Csv withNominalFields(String... fields) {
        nominalFieldHints = new HashSet<>(Arrays.asList(fields));
        return this;
    }

    public Csv withDefaultType(VarType defaultType) {
        this.defaultType = defaultType;
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
        List<String> names = new ArrayList<>();
        List<Var> vars = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
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
                        if (indexFieldHints.contains(colName)) {
                            vars.add(Index.newEmpty());
                            continue;
                        }
                        if (numericFieldHints.contains(colName)) {
                            vars.add(Numeric.newEmpty());
                            continue;
                        }
                        if (nominalFieldHints.contains(colName)) {
                            vars.add(new Nominal());
                            continue;
                        }
                        // default getType
                        switch (defaultType) {
                            case NOMINAL:
                                vars.add(new Nominal());
                                break;
                            case NUMERIC:
                                vars.add(Numeric.newEmpty());
                                break;
                            case INDEX:
                                vars.add(Index.newEmpty());
                                break;
                        }
                    }
                }

                if (rows < startRow) {
                    rows++;
                    continue;
                }
                if (rows == endRow) break;
                rows++;
                for (int i = 0; i < names.size(); i++) {
                    if (row.size() <= i || naValues.contains(row.get(i))) {
                        vars.get(i).addMissing();
                        continue;
                    }
                    String value = row.get(i);
                    Var v = vars.get(i);
                    switch (v.type()) {
                        case INDEX:
                            Integer intValue;
                            try {
                                intValue = Integer.parseInt(value);
                                v.addIndex(intValue);
                            } catch (Throwable ex) {
                                // can't parse, try numeric
                                Double fallbackNumeric;
                                try {
                                    fallbackNumeric = Double.parseDouble(value);
                                    Numeric num = Numeric.newEmpty();
                                    for (int j = 0; j < v.rowCount(); j++) {
                                        num.addValue(v.index(j));
                                    }
                                    num.addValue(fallbackNumeric);
                                    vars.set(i, num);
                                    continue;

                                } catch (Throwable ex2) {
                                    // can't parse, use nominal
                                    Nominal nom = new Nominal();
                                    for (int j = 0; j < v.rowCount(); j++) {
                                        nom.addLabel(String.valueOf(v.index(j)));
                                    }
                                    nom.addLabel(value);
                                    vars.set(i, nom);
                                    continue;
                                }
                            }
                            break;
                        case NUMERIC:
                            Double numValue;
                            try {
                                numValue = Double.parseDouble(value);
                                v.addValue(numValue);
                            } catch (Throwable ex) {
                                // can't parse, use nominal
                                Nominal nom = new Nominal();
                                for (int j = 0; j < v.rowCount(); j++) {
                                    nom.addLabel(String.valueOf(v.value(j)));
                                }
                                nom.addLabel(value);
                                vars.set(i, nom);
                                continue;
                            }
                            break;
                        case NOMINAL:
                            v.addLabel(value);
                            break;
                    }
                }
            }
        }
        return new SolidFrame(rows - startRow, vars, names, null);
    }

    public List<String> parseLine(String line) {
        List<String> data = new ArrayList<>();
        int start = 0;
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
            data.add(clean(line.substring(start, end)));
            start = end + 1;
        }
        return data;
    }

    /**
     * Clean the string token. - remove trailing and leading spaces, before and
     * after removing quotas - remove leading and trailing quotas - remove
     * escape quota character
     *
     * @param tok
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
                for (int i = 0; i < df.colNames().length; i++) {
                    if (i != 0) {
                        writer.append(separatorChar);
                    }
                    writer.append(df.colNames()[i]);
                }
                writer.append("\n");
            }
            DecimalFormat format = new DecimalFormat("0.###############################");
            for (int i = 0; i < df.rowCount(); i++) {
                for (int j = 0; j < df.colCount(); j++) {
                    if (j != 0) {
                        writer.append(separatorChar);
                    }
                    if (df.col(j).missing(i)) {
                        writer.append("?");
                        continue;
                    }
                    if (df.col(j).type().isNominal()) {
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
}
