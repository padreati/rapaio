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

package rapaio.math.linear;

import rapaio.WS;
import rapaio.core.Printable;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public interface M extends Serializable, Printable {

    int rows();

    int cols();

    double get(int i, int j);

    void set(int i, int j, double value);

    static M newEmpty(int rows, int cols) {
        return new SolidM(rows, cols);
    }

    static M newFill(int rows, int cols, double fill) {
        if (fill == 0) {
            return newEmpty(rows, cols);
        }
        M m = new SolidM(rows, cols);
        for (int i = 0; i < m.rows(); i++) {
            for (int j = 0; j < m.cols(); j++) {
                m.set(i, j, fill);
            }
        }
        return m;
    }

    default M t() {
        return new TransposeM(this);
    }

    default M mapRows(int... indexes) {
        return new MappedM(this, true, indexes);
    }

    default M removeRows(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] rows = new int[rows() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rows(); i++) {
            if (rem.contains(i))
                continue;
            rows[pos++] = i;
        }
        return new MappedM(this, true, rows);
    }

    default M mapCols(int... indexes) {
        return new MappedM(this, false, indexes);
    }

    default M removeCols(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[cols() - rem.size()];
        int pos = 0;
        for (int i = 0; i < cols(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new MappedM(this, false, cols);
    }

    default void buildSummary(StringBuilder sb) {

        DecimalFormat f = new DecimalFormat();
        f.setMaximumFractionDigits(3);
        f.setMinimumFractionDigits(3);
        f.setMinimumIntegerDigits(1);

        String[][] m = new String[rows()][cols()];
        int max = 0;
        for (int i = 0; i < rows(); i++) {
            for (int j = 0; j < cols(); j++) {
                m[i][j] = f.format(get(i, j));
                max = Math.max(max, m[i][j].length() + 2);
            }
        }
        max = Math.max(max, String.format("[,%d]", rows()).length());
        max = Math.max(max, String.format("[%d,]", cols()).length());

        String fm = "%-" + max + "s";

        int hCount = (int) Math.floor(WS.getPrinter().getTextWidth() / (double) max);
        int vCount = Math.min(cols(), 20) + 1;
        int hLast = 0;
        while (true) {

            // take vertical stripes
            if (hLast >= cols())
                break;

            int hStart = hLast;
            int hEnd = Math.min(hLast + hCount, cols());
            int vLast = 0;

            while (true) {

                // print rows
                if (vLast >= rows())
                    break;

                int vStart = vLast;
                int vEnd = Math.min(vLast + vCount, rows());

                for (int i = vStart; i <= vEnd; i++) {
                    for (int j = hStart; j <= hEnd; j++) {
                        if (i == vStart && j == hStart) {
                            WS.print(String.format("%" + (max + 1) + "s", ""));
                            continue;
                        }
                        if (i == vStart) {
                            WS.print(String.format("[ ,%" + (max - 4) + "d]", j - 1));
                            continue;
                        }
                        if (j == hStart) {
                            WS.print(String.format("[%" + (max - 4) + "d, ]", i - 1));
                            continue;
                        }
                        WS.print(String.format("%" + max + "s", m[i - 1][j - 1]));
                    }
                    WS.print("\n");
                }
                WS.print("\n");
                vLast = vEnd;
            }
            hLast = hEnd;
        }
    }

}
