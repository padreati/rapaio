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

package rapaio.core.tools;

import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.printer.Printable;

import java.util.HashMap;
import java.util.Map;

/**
 * Frequency table which allows one to build counts, weight sums or percentiles one, two
 * or more variables.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/20/15.
 */
public class FTable implements Printable {

    private final String[] xLevels;
    private final String[] yLevels;
    private final double[][] values;
    private final Map<String, Integer> xRevert;
    private final Map<String, Integer> yRevert;

    public static FTable newFromLevels(Var x, Var y) {
        VarType xtype = x.type();
        VarType ytype = y.type();
        if (!(xtype.equals(VarType.BINARY) || xtype.equals(VarType.NOMINAL) || xtype.equals(VarType.ORDINAL))) {
            throw new IllegalArgumentException("x: " + x.name() + ", with type: " + x.type().name() + " is not legal");
        }
        if (!(ytype.equals(VarType.BINARY) || ytype.equals(VarType.NOMINAL) || ytype.equals(VarType.ORDINAL))) {
            throw new IllegalArgumentException("x: " + x.name() + ", with type: " + x.type().name() + " is not legal");
        }
        FTable ft = new FTable(x.levels(), y.levels());
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                ft.values[i][j] += 1.0;
            }
        }
        return ft;
    }

    private FTable(String[] xLevels, String[] yLevels) {
        this.xLevels = xLevels;
        this.yLevels = yLevels;
        this.values = new double[xLevels.length][yLevels.length];
        this.xRevert = new HashMap<>();
        this.yRevert = new HashMap<>();
        for (int i = 0; i < xLevels.length; i++) {
            xRevert.put(xLevels[i], i);
        }
        for (int i = 0; i < yLevels.length; i++) {
            yRevert.put(yLevels[i], i);
        }
    }

    public void increment(int x, int y, double value) {
        values[x][y] += value;
    }

    public void set(int x, int y, double value) {
        values[x][y] = value;
    }

    public double get(int x, int y) {
        return values[x][y];
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        return sb.toString();
    }
}
