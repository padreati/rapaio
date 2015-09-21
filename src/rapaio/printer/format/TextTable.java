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
    private Function<String, String> stringFormatter = x -> x;
    private Function<Double, String> doubleFormatter = WS::formatFlex;
    private Function<Integer, String> intFormatter = String::valueOf;
    private final String[][] values;

    private TextTable(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        values = new String[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                values[i][j] = "";
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Function<String, String> getStringFormatter() {
        return stringFormatter;
    }

    public void setStringFormatter(Function<String, String> stringFormatter) {
        this.stringFormatter = stringFormatter;
    }

    public Function<Double, String> getDoubleFormatter() {
        return doubleFormatter;
    }

    public void setDoubleFormatter(Function<Double, String> doubleFormatter) {
        this.doubleFormatter = doubleFormatter;
    }

    public Function<Integer, String> getIntFormatter() {
        return intFormatter;
    }

    public void setIntFormatter(Function<Integer, String> intFormatter) {
        this.intFormatter = intFormatter;
    }

    public String getText(int row, int col) {
        return values[row][col];
    }

    public void setText(int row, int col, double x) {
        setText(row, col, x, doubleFormatter);
    }

    public void setText(int row, int col, double x, Function<Double, String> fmt) {
        values[row][col] = fmt.apply(x);
    }

    public void setText(int row, int col, int x) {
        setText(row, col, x, intFormatter);
    }

    public void setText(int row, int col, int x, Function<Integer, String> fmt) {
        values[row][col] = fmt.apply(x);
    }

    public void setText(int row, int col, String x) {
        setText(row, col, x, stringFormatter);
    }

    public void setText(int row, int col, String x, Function<String, String> fmt) {
        values[row][col] = fmt.apply(x);
    }


    @Override
    public String summary() {


        return null;
    }
}
