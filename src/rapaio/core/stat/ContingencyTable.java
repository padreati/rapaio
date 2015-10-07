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

package rapaio.core.stat;

import rapaio.data.*;
import rapaio.printer.Printable;
import rapaio.ws.Summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/18/15.
 */
public class ContingencyTable implements Printable {

    private final String xName;
    private final String yName;
    private final String[] xNames;
    private final String[] yNames;
    private final int[][] freq;
    private final double[][] perc;

    public ContingencyTable(Var x, Var y) {
        List<VarType> varTypes = Arrays.asList(VarType.NOMINAL, VarType.ORDINAL, VarType.BINARY);
        if (!varTypes.contains(x.type()))
            throw new IllegalArgumentException("Cannot build contingency table for non-nominal x variable " + x.name());
        if (!varTypes.contains(x.type()))
            throw new IllegalArgumentException("Cannot build contingency table for non-nominal y variable " + y.name());
        if (x.rowCount() != y.rowCount())
            throw new IllegalArgumentException("Cannot buid contingency table for variables of different size");

        xName = x.name();
        yName = y.name();
        xNames = x.levels().clone();
        yNames = y.levels().clone();
        freq = new int[xNames.length][yNames.length];
        perc = new double[xNames.length][yNames.length];

        for (int i = 0; i < x.rowCount(); i++) {
            freq[x.index(i)][y.index(i)]++;
        }
        for (int i = 0; i < xNames.length; i++) {
            for (int j = 0; j < yNames.length; j++) {
                perc[i][j] = freq[i][j] * 1.0 / x.rowCount();
            }
        }
    }

    public int[][] frequencies() {
        return freq;
    }

    public double[][] percents() {
        return perc;
    }

    private Frame builFrameFreq() {
        List<Var> vars = new ArrayList<>();
        Nominal left = Nominal.newCopyOf(yNames).withName("");
        vars.add(left);
        for (String name : xNames) {
            vars.add(Index.newEmpty().withName(name));
        }
        // write y titles
        for (int i = 0; i < xNames.length; i++) {
            for (int j = 0; j < yNames.length; j++) {
                vars.get(i + 1).addIndex(freq[i][j]);
            }
        }
        return SolidFrame.newWrapOf(vars);
    }

    private Frame builFramePerc() {
        List<Var> vars = new ArrayList<>();
        Nominal left = Nominal.newCopyOf(yNames).withName("");
        vars.add(left);
        for (String name : xNames) {
            vars.add(Numeric.newEmpty().withName(name));
        }
        // write y titles
        for (int i = 0; i < xNames.length; i++) {
            for (int j = 0; j < yNames.length; j++) {
                vars.get(i + 1).addValue(perc[i][j]);
            }
        }
        return SolidFrame.newWrapOf(vars);
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("> ContingencyTable ( x=").append(xName).append(", y=").append(yName).append(" )\n");
        sb.append("Frequency table:\n");
        sb.append(Summary.headString(builFrameFreq()));
        sb.append("Percentage table:\n");
        sb.append(Summary.headString(builFramePerc()));
        sb.append("\n");
        return sb.toString();
    }
}
