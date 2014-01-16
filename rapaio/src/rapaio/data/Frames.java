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

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public final class Frames {

    /**
     * Build a frame which has only numeric columns and values are filled with 0
     * (no missing values).
     *
     * @param rows     number of rowCount
     * @param colNames column names
     * @return the new built frame
     */
    public static Frame newMatrixFrame(int rows, String... colNames) {
        Vector[] vectors = new Vector[colNames.length];
        for (int i = 0; i < colNames.length; i++) {
            vectors[i] = new Numeric(new double[rows]);
        }
        return new SolidFrame(rows, vectors, colNames);
    }

    public static Frame newMatrixFrame(int rows, List<String> colNames) {
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < colNames.size(); i++) {
            vectors.add(new Numeric(new double[rows]));
        }
        return new SolidFrame(rows, vectors, colNames);
    }

    public static Frame solidCopy(Frame df) {
        int len = df.rowCount();
        List<Vector> vectors = new ArrayList<>();
        List<String> names = new ArrayList<>();

        for (int i = 0; i < df.colCount(); i++) {
            Vector src = df.col(i);
            if (src.type().isNominal()) {
                vectors.add(new Nominal(len, df.col(i).dictionary()));
                names.add(df.colNames()[i]);
                for (int j = 0; j < df.rowCount(); j++) {
                    vectors.get(i).setLabel(j, src.label(j));
                }
            }
            if (src.type().isNumeric()) {
                vectors.add(new Numeric(len));
                names.add(df.colNames()[i]);
                for (int j = 0; j < df.rowCount(); j++) {
                    vectors.get(i).setValue(j, src.value(j));
                }
            }
        }
        return new SolidFrame(len, vectors, names);
    }

    public static Frame addCol(Frame df, Vector col, String name, int position) {
        if (df.rowCount() != col.rowCount()) {
            throw new IllegalArgumentException("frame and col have different row counts");
        }
        if (df.isMappedFrame()) {
            throw new IllegalArgumentException("operation not allowed on mapped frames");
        }
        List<Vector> vectors = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (String colName : df.colNames()) {
            names.add(colName);
            vectors.add(df.col(colName));
        }
        vectors.add(position, col);
        names.add(position, name);
        return new SolidFrame(df.rowCount(), vectors, names);
    }
}
