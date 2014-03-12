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

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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
	private VectorType defaultTypeHint = VectorType.NOMINAL;
	private int startRow = 0;
	private int endRow = Integer.MAX_VALUE;

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

	public VectorType getDefaultTypeHint() {
		return defaultTypeHint;
	}

	public int getStartRow() {
		return startRow;
	}

	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public void setEndRow(int endRow) {
		this.endRow = endRow;
	}

	public void setDefaultTypeHint(VectorType defaultTypeHint) {
		this.defaultTypeHint = defaultTypeHint;
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
		List<Vector> vectors = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
			if (hasHeader) {
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
					for (int i = 0; i < names.size(); i++) {
						String colName = names.get(i);
						if (indexFieldHints.contains(colName)) {
							vectors.add(new Index(0, 0, 0));
							continue;
						}
						if (numericFieldHints.contains(colName)) {
							vectors.add(new Numeric());
							continue;
						}
						if (nominalFieldHints.contains(colName)) {
							vectors.add(new Nominal());
							continue;
						}
						// default getType
						switch (defaultTypeHint) {
							case NOMINAL:
								vectors.add(new Nominal());
								break;
							case NUMERIC:
								vectors.add(new Numeric());
								break;
							case INDEX:
								vectors.add(new Index());
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
					if (row.size() <= i || "?".equals(row.get(i)) || "NA".equals(row.get(i))) {
						vectors.get(i).addMissing();
						continue;
					}
					String value = row.get(i);
					Vector v = vectors.get(i);
					switch (v.getType()) {
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
									Numeric num = new Numeric();
									for (int j = 0; j < v.getRowCount(); j++) {
										num.addValue(v.getIndex(j));
									}
									num.addValue(fallbackNumeric);
									vectors.set(i, num);
									continue;

								} catch (Throwable ex2) {
									// can't parse, use nominal
									Nominal nom = new Nominal();
									for (int j = 0; j < v.getRowCount(); j++) {
										nom.addLabel(String.valueOf(v.getIndex(j)));
									}
									nom.addLabel(value);
									vectors.set(i, nom);
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
								for (int j = 0; j < v.getRowCount(); j++) {
									nom.addLabel(String.valueOf(v.getValue(j)));
								}
								nom.addLabel(value);
								vectors.set(i, nom);
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
		return new SolidFrame(rows - startRow, vectors, names);
	}

	/**
	 * Parses a line from csv file according with the configured setting for the
	 * parse. E.g. separates columns by getCol separator, but not by the getColCount
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
