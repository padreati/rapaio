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

package rapaio.printer.format;

import rapaio.data.VarType;
import rapaio.printer.Printable;
import rapaio.sys.WS;

import java.util.Formatter;
import java.util.List;
import java.util.function.Function;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/16/14.
 */
@Deprecated
public class TextTable implements Printable {

    public static TextTable newEmpty(int rows, int cols) {
        return new TextTable(rows, cols);
    }

    private final int rows;
    private final int cols;
    private final String[][] values;
    private final int[][] mergeRows;
    private final int[][] align;

    private TextTable(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        values = new String[rows][cols];
        mergeRows = new int[rows][cols];
        align = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = "";
                mergeRows[i][j] = 1;
                align[i][j] = -1;
            }
        }
    }

    public int rows() {
        return rows;
    }

    public int cols() {
        return cols;
    }

    public String get(int row, int col) {
        return values[row][col];
    }

    public void set(int row, int col, String x) {
        values[row][col] = x;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();

        return sb.toString();
    }
}
