/*
 * Copyright 2013 Aurelian Tutuianu
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
    //
    private Pattern pattern;
    //
    private boolean header = true;
    private char colSeparator = ',';
    private char headerSeparator = ',';
    private boolean trimSpaces = true;
    private boolean hasQuotas = true;
    private String doubleQuotas = "\"\"";
    private char decimalPoint = '.';

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public char getColSeparator() {
        return colSeparator;
    }

    public void setColSeparator(char colSeparator) {
        this.colSeparator = colSeparator;
    }

    public char getHeaderSeparator() {
        return headerSeparator;
    }

    public void setHeaderSeparator(char headerSeparator) {
        this.headerSeparator = headerSeparator;
    }

    public boolean getHasQuotas() {
        return hasQuotas;
    }

    public void setHasQuotas(boolean hasQuotas) {
        this.hasQuotas = hasQuotas;
    }

    public String getDoubleQuotas() {
        return doubleQuotas;
    }

    public void setDoubleQuotas(String doubleQuotas) {
        this.doubleQuotas = doubleQuotas;
    }

    public boolean isTrimSpaces() {
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
        return read(name, new FileInputStream(file));
    }

    public Frame read(String name, InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            List<String> names = new ArrayList<>();
            if (header) {
                String line = reader.readLine();
                if (line == null) {
                    return null;
                }
                names = readHeader(line);
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

    private List<String> readHeader(String line) {
        pattern = buildPattern(getHeaderSeparator());
        List<String> data = new ArrayList<>();
        String[] tokens = pattern.split(line, -1);
        for (String token : tokens) {
            data.add(clean(token));
        }
        return data;
    }

    private List<String> readData(String line) {
        pattern = buildPattern(getColSeparator());
        List<String> data = new ArrayList<>();
        String[] tokens = pattern.split(line, -1);
        for (String token : tokens) {
            data.add(clean(token));
        }
        return data;
    }

    private String clean(String tok) {
        if (isTrimSpaces()) {
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
        if (isTrimSpaces()) {
            tok = tok.trim();
        }
        return tok;
    }

    private Pattern buildPattern(char separator) {
        return Pattern.compile(separator + "(?=([^\"]*\"[^\"]*\")*(?![^\"]*\"))");
    }

    private Frame buildFrame(String name, List<String> names, List<String> rows) {
        int cols = names.size();
        List<List<String>> splitted = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            List<String> row = readData(rows.get(i));
            cols = Math.max(cols, row.size());
            splitted.add(row);
        }
        Vector[] vectors = new Vector[cols];
        // process data, one column at a time
        for (int i = 0; i < cols; i++) {
            // TODO complete
            HashSet<String> dict = new HashSet<>();
            for (int j = 0; j < rows.size(); j++) {
                if (splitted.get(j).size() > i) {
                    dict.add(splitted.get(j).get(i));
                }
            }
            vectors[i] = new NominalVector(names.get(i), rows.size(), dict);
            for (int j = 0; j < rows.size(); j++) {
                if (splitted.get(j).size() > i) {
                    vectors[i].setLabel(j, splitted.get(j).get(i));
                }
            }
        }
        return new SolidFrame(name, rows.size(), vectors);
    }

    public void write(Frame df, String fileName) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            if (isHeader()) {
                writer.append(df.getColNames()[0]);
                for (int i = 1; i < df.getColNames().length; i++) {
                    writer.append(getHeaderSeparator()).append(df.getColNames()[i]);
                }
                writer.append("\n");
            }
            for (int i = 0; i < df.getRowCount(); i++) {
                // TODO
                for (int j = 0; j < df.getColCount(); j++) {
                    if (j != 0) {
                        writer.append(getColSeparator());
                    }
                    if (df.getCol(j).isNominal()) {
                        writer.append(df.getLabel(i, j));
                    } else {
                        writer.append(String.format("%.20f", df.getValue(i, j)));
                    }
                }
                writer.append("\n");
            }
            writer.flush();
        }
    }
}
