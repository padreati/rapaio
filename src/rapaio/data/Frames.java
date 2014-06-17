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

package rapaio.data;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;

import java.io.Serializable;
import java.util.*;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public final class Frames implements Serializable {

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of getRowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrix(int rows, String... colNames) {
        Var[] vars = new Var[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            vars[i] = Numeric.newFill(rows, 0);
        }
        return new SolidFrame(rows, vars, colNames, null);
    }

    public static Frame newMatrix(int rows, List<String> colNames) {
        List<Var> vars = new ArrayList<>();
        colNames.stream().forEach(n -> vars.add(new Numeric(rows, rows, 0)));
        return new SolidFrame(rows, vars, colNames, null);
    }

    public static Frame solidCopy(Frame df) {
        int len = df.rowCount();
        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (int i = 0; i < df.colCount(); i++) {
            Var src = df.col(i);
            if (src.type().isNominal()) {
                vars.add(Nominal.newEmpty(len, df.col(i).dictionary()));
                names.add(df.colNames()[i]);
                for (int j = 0; j < df.rowCount(); j++) {
                    vars.get(i).setLabel(j, src.label(j));
                }
            }
            if (src.type().isNumeric()) {
                vars.add(Numeric.newFill(len, 0));
                names.add(df.colNames()[i]);
                for (int j = 0; j < df.rowCount(); j++) {
                    vars.get(i).setValue(j, src.value(j));
                }
            }
        }
        return new SolidFrame(len, vars, names, df.weights());
    }

    public static Frame addCol(Frame df, Var col, String name, int position) {
        if (df.rowCount() != col.rowCount()) {
            throw new IllegalArgumentException("frame and getCol have different row counts");
        }
        if (df.isMappedFrame()) {
            throw new IllegalArgumentException("operation not allowed on mapped frames");
        }
        List<Var> vars = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (String colName : df.colNames()) {
            names.add(colName);
            vars.add(df.col(colName));
        }
        vars.add(position, col);
        names.add(position, name);
        return new SolidFrame(df.rowCount(), vars, names, df.weights());
    }

    /**
     * Scale all numeric columns by substracting mean and dividing by variance.
     * Additionally it replaces missing values with 0.
     *
     * @param df
     */
    public static void scale(Frame df, String exceptCols) {
        Set<String> except = new HashSet<>();
        if (exceptCols != null && !exceptCols.isEmpty())
            Collections.addAll(except, exceptCols.split(",", -1));
        for (int i = 0; i < df.colCount(); i++) {
            if (df.col(i).type().isNumeric() && !except.contains(df.colNames()[i])) {
                double mean = new Mean(df.col(i)).getValue();
                double sd = StrictMath.sqrt(new Variance(df.col(i)).getValue());

                if (mean != mean || sd != sd) {
                    throw new RuntimeException("mean or sd is NaN");
                }
                if (sd == 0) continue;

                for (int j = 0; j < df.rowCount(); j++) {
                    if (df.missing(j, i)) {
                        df.setValue(j, i, 0);
                        continue;
                    }
                    df.setValue(j, i, (df.value(j, i) - mean) / sd);
                }
            }
        }
    }
}
