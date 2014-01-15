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
import rapaio.data.Vector;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CsvPersistence {

    private boolean trimSpaces = true;
    private boolean hasHeader = true;
    private boolean hasQuotas = true;
    private char colSeparator = ',';
    private char escapeQuotas = '\"';
    private final HashSet<String> numericFieldHints = new HashSet<>();
    private final HashSet<String> indexFieldHints = new HashSet<>();
    private final HashSet<String> nominalFieldHints = new HashSet<>();

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

    public HashSet<String> getNumericFieldHints() {
        return numericFieldHints;
    }

    public HashSet<String> getIndexFieldHints() {
        return indexFieldHints;
    }

    public HashSet<String> getNominalFieldHints() {
        return nominalFieldHints;
    }

    public Frame read(String fileName) throws IOException {
        return read(new MyReader(fileName));
    }

    public Frame read(Class<?> clazz, String resource) throws IOException {
        return read(new MyReader(clazz, resource));
    }

    private Frame read(MyReader myReader) throws IOException {
        List<String> names = new ArrayList<>();
        int cols;

        try (BufferedReader reader = myReader.reader()) {
            if (hasHeader) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = parseLine(line);
            }
            cols = names.size();
            int len = 0;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                List<String> row = parseLine(line);
                cols = Math.max(cols, row.size());
                len++;
                if (len > 100)
                    break; // enough is enough
            }
        }
        for (int i = names.size(); i < cols; i++) {
            names.add("V" + (i + 1));
        }

        // learn nominal dictionaries
        HashMap<String, HashSet<String>> dictionaries = new HashMap<>();
        HashMap<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            String colName = names.get(i);
            indexes.put(colName, i);
            if (indexFieldHints.contains(colName) || numericFieldHints.contains(colName)) {
                continue;
            }
            dictionaries.put(colName, new HashSet<String>());
        }
        int rows = 0;
        try (BufferedReader reader = myReader.reader()) {
            if (hasHeader) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
            }
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (!dictionaries.isEmpty()) {
                    List<String> row = parseLine(line);
                    for (Map.Entry<String, HashSet<String>> entry : dictionaries.entrySet()) {
                        if (row.size() > indexes.get(entry.getKey())) {
                            String label = row.get(indexes.get(entry.getKey()));
                            if (!label.isEmpty())
                                entry.getValue().add(label);
                        }
                    }
                }
                rows++;
            }
        }

        // learn frame
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < cols; i++) {
            String colName = names.get(i);
            if (indexFieldHints.contains(colName)) {
                vectors.add(Vectors.newIdx(rows));
                continue;
            }
            if (numericFieldHints.contains(colName)) {
                vectors.add(new NumVector(rows));
                continue;
            }
            vectors.add(new NomVector(rows, dictionaries.get(colName)));
        }
        Frame df = new SolidFrame(rows, vectors, names);

        // process data, one row at a time
        rows = 0;
        try (BufferedReader reader = myReader.reader()) {
            if (hasHeader) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
            }
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                List<String> row = parseLine(line);
                for (String colName : indexFieldHints) {
                    if (!indexes.containsKey(colName))
                        continue;
                    if ("?".equals(row.get(indexes.get(colName)))) {
                        df.getCol(colName).setMissing(rows);
                        continue;
                    }
                    df.getCol(colName).setIndex(rows, Integer.parseInt(row.get(indexes.get(colName))));
                }
                for (String colName : numericFieldHints) {
                    if (!indexes.containsKey(colName))
                        continue;
                    if ("?".equals(row.get(indexes.get(colName)))) {
                        df.getCol(colName).setMissing(rows);
                        continue;
                    }
                    df.getCol(colName).setValue(rows, Double.parseDouble(row.get(indexes.get(colName))));
                }
                for (String colName : dictionaries.keySet()) {
                    if (!indexes.containsKey(colName))
                        continue;
                    if (row.size() <= indexes.get(colName))
                        continue;
                    String label = row.get(indexes.get(colName));
                    if ("?".equals(label)) {
                        df.getCol(colName).setMissing(rows);
                        continue;
                    }
                    if (!label.isEmpty())
                        df.getCol(colName).setLabel(rows, label);
                }
                rows++;
            }
        }
        // return solid frame
        return df;
    }

    /**
     * Parses a line from csv file according with the configured setting for the
     * parse. E.g. separates columns by col separator, but not by the cols
     * separators inside quotas, if quota is configured.
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
     * Clean the string token. - remove trailing and leading spaces, before and
     * after removing quotas - remove leading and trailing quotas - remove
     * escape quota character
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
        if (getHasQuotas()) {
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
        }
        if (trimSpaces()) {
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
            if (hasHeader()) {
                for (int i = 0; i < df.getColNames().length; i++) {
                    if (i != 0) {
                        writer.append(getColSeparator());
                    }
                    writer.append(df.getColNames()[i]);
                }
                writer.append("\n");
            }
            DecimalFormat format = new DecimalFormat("0.###############################");
            for (int i = 0; i < df.getRowCount(); i++) {
                for (int j = 0; j < df.getColCount(); j++) {
                    if (j != 0) {
                        writer.append(getColSeparator());
                    }
                    if (df.getCol(j).isMissing(i)) {
                        writer.append("?");
                        continue;
                    }
                    if (df.getCol(j).getType().isNominal()) {
                        writer.append(unclean(df.getLabel(i, j)));
                    } else {
                        writer.append(format.format(df.getValue(i, j)));
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

class MyReader {

    private final String fileName;
    private final Class<?> clazz;
    private final String resource;

    public MyReader(String fileName) {
        this.fileName = fileName;
        this.clazz = null;
        this.resource = null;
    }

    public MyReader(Class<?> clazz, String resource) {
        this.fileName = null;
        this.clazz = clazz;
        this.resource = resource;
    }

    public BufferedReader reader() throws FileNotFoundException {
        if (fileName != null)
            return new BufferedReader(new FileReader(fileName));
        return new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(resource)));
    }
}
