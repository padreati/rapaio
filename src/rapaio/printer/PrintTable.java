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

package rapaio.printer;

import rapaio.data.Frame;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class PrintTable {

    final String[][] values;
    final String[] headers;

    private NumberFormat valueFormat = new DecimalFormat("0.######");
    private NumberFormat indexFormat = new DecimalFormat("0");

    public PrintTable(int rows, String[] headers) {
        this.headers = Arrays.copyOf(headers, headers.length);
        this.values = new String[rows][headers.length];
    }

    public PrintTable(Frame df) {
        this.headers = df.colNames();
        this.values = new String[df.rowCount()][df.colCount()];
    }
}
