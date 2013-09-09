/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvPersistence {

    private static final String NOMINAL_MISSING_VALUE = "?";
    private boolean trimSpaces = true;
    private boolean hasHeader = true;
    private boolean hasQuotas = true;
    private char colSeparator = ',';
    private char escapeQuotas = '\"';
    private char decimalPoint = '.';

    public boolean hasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public char getColSeparator() {
        return colSeparator;
    }

    public void setColSeparator(char colSeparator) {
        this.colSeparator = colSeparator;
    }

    public boolean getHasQuotas() {
        return hasQuotas;
    }

    public void setHasQuotas(boolean hasQuotas) {
        this.hasQuotas = hasQuotas;
    }

    public char getEscapeQuotas() {
        return escapeQuotas;
    }

    public void setEscapeQuotas(char escapeQuotas) {
        this.escapeQuotas = escapeQuotas;
    }

    public boolean trimSpaces() {
        return trimSpaces;
    }

    public void setTrimSpaces(boolean trimSpaces) {
        this.trimSpaces = trimSpaces;
    }

    public char getDecimalPoint() {
        return decimalPoint;
    }

    public void setDecimalPoint(char decimalPoint) {
        this.decimalPoint = decimalPoint;
    }

    public Frame read(String name, String fileName) throws IOException {
        return read(name, new File(fileName));
    }

    public Frame read(String name, File file) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return read(name, is);
        }
    }

    public Frame read(String name, InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<String> names = new ArrayList<>();
            if (hasHeader) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = parseLine(line);
            }
            List<String> rows = new ArrayList<>();
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                rows.add(line);
            }
            return buildFrame(name, names, rows);
        }
    }

    /**
     * Parses a line from csv file according with the configured setting for the parse.
     * E.g. separates columns by col separator, but not by the cols separators inside quotas,
     * if quota is  configured.
     *
     * @param line
     * @return
     */
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
                if (inQuotas && ch == getEscapeQuotas()) {
                    if (end < line.length() && line.charAt(end) == '\"') {
                        end++;
                        continue;
                    }
                }
                if (inQuotas && ch == '"') {
                    if (getEscapeQuotas() == '\"') {
                        if (end < line.length() && line.charAt(end) == '\"') {
                            end++;
                            continue;
                        }
                    }
                    inQuotas = false;
                    continue;
                }
                if (!inQuotas && (ch == getColSeparator())) {
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
     * Clean the string token.
     * - remove trailing and leading spaces, before and after removing quotas
     * - remove leading and trailing quotas
     * - remove escape quota character
     *
     * @param tok
     * @return
     */
    private String clean(String tok) {
        if (trimSpaces()) {
            tok = tok.trim();
        }
        if (getHasQuotas() && !tok.isEmpty()) {
            if (tok.charAt(0) == '\"') {
                tok = tok.substring(1);
            }
            if (tok.charAt(tok.length() - 1) == '\"') {
                tok = tok.substring(0, tok.length() - 1);
            }
        }
        char[] line = new char[tok.length()];
        int len = 0;
        for (int i = 0; i < tok.length(); i++) {
            if (len < tok.length() - 1 && tok.charAt(i) == getEscapeQuotas() && tok.charAt(i + 1) == '\"') {
                line[len++] = '\"';
                i++;
                continue;
            }
            line[len++] = tok.charAt(i);
        }
        tok = String.valueOf(line, 0, len);
        if (trimSpaces()) {
            tok = tok.trim();
        }
        return tok;
    }

    private Frame buildFrame(String name, List<String> names, List<String> rows) {
        int cols = names.size();
        List<List<String>> split = new ArrayList<>();
        for (String line : rows) {
            List<String> row = parseLine(line);
            cols = Math.max(cols, row.size());
            split.add(row);
        }
        for (int i = names.size(); i < cols; i++) {
            names.add("V" + (i + 1));
        }
        Vector[] vectors = new Vector[cols];
        // process data, one column at a time
        for (int i = 0; i < cols; i++) {
            // TODO complete with building other than nominal
            HashSet<String> dict = new HashSet<>();
            for (int j = 0; j < rows.size(); j++) {
                if (split.get(j).size() > i) {
                    dict.add(split.get(j).get(i));
                }
            }
            vectors[i] = new NominalVector(names.get(i), rows.size(), dict);
            for (int j = 0; j < rows.size(); j++) {
                if (split.get(j).size() > i) {
                    vectors[i].setLabel(j, split.get(j).get(i));
                }
            }
        }
        return new SolidFrame(name, rows.size(), vectors);
    }

    public void write(Frame df, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            if (hasHeader()) {
                for (int i = 0; i < df.getColNames().length; i++) {
                    if (i != 0) {
                        writer.append(getColSeparator());
                    }
                    writer.append(df.getColNames()[i]);
                }
                writer.append("\n");
            }
            for (int i = 0; i < df.getRowCount(); i++) {
                for (int j = 0; j < df.getColCount(); j++) {
                    if (j != 0) {
                        writer.append(getColSeparator());
                    }
                    if (df.getCol(j).isNominal()) {
                        writer.append(unclean(df.getLabel(i, j)));
                    } else {
                        writer.append(String.format("%.20f", df.getValue(i, j)));
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
                line[len++] = getEscapeQuotas();
            }
            line[len++] = label.charAt(i);
        }
        label = String.valueOf(line, 0, len);
        if (getHasQuotas()) {
            label = "\"" + label + "\"";
        }
        return label;
    }
}
