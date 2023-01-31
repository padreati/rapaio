/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.correlation;

import java.io.Serial;
import java.util.Arrays;

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/4/19.
 */
public abstract class AbstractCorrelation implements Correlation {

    @Serial
    private static final long serialVersionUID = 1291660783599973889L;
    protected final DistanceMatrix distanceMatrix;
    protected final int rowCount;

    public AbstractCorrelation(Var[] vars, String[] names) {
        if (vars.length <= 1) {
            throw new IllegalArgumentException("Correlation can be computed only between two variables.");
        }
        for (int i = 1; i < vars.length; i++) {
            if (vars[i - 1].size() != vars[i].size()) {
                throw new IllegalArgumentException("Variables does not have the same size.");
            }
        }
        rowCount = Arrays.stream(vars).mapToInt(Var::size).min().orElse(0);
        distanceMatrix = DistanceMatrix.empty(names);
    }

    @Override
    public DistanceMatrix matrix() {
        return distanceMatrix;
    }

    @Override
    public double singleValue() {
        return distanceMatrix.get(0, 1);
    }

    protected abstract String corrName();

    protected abstract String corrDescription();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(corrName()).append(Arrays.deepToString(distanceMatrix.names()));
        int pos = 10;

        sb.append(" = [");
        for (int i = 0; i < distanceMatrix.names().length; i++) {
            boolean flag = pos >= 0;
            if (!flag) {
                sb.append("...");
                break;
            }
            sb.append('[');
            for (int j = 0; j < distanceMatrix.names().length; j++) {
                if (pos >= 0) {
                    sb.append(Format.floatFlex(distanceMatrix.get(i, j)));
                    if (j < distanceMatrix.names().length - 1) {
                        sb.append(",");
                    }
                    pos--;
                    continue;
                }
                sb.append("...");
                break;
            }
            sb.append("]");
            if (i < distanceMatrix.names().length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        if (distanceMatrix.names().length == 2) {
            summaryTwo(sb);
        } else {
            summaryMore(sb, printer, options);
        }
        return sb.toString();
    }

    private void summaryTwo(StringBuilder sb) {
        sb.append(String.format("> %s[%s, %s] - %s\n", corrName(), distanceMatrix.name(0), distanceMatrix.name(1), corrDescription()));
        sb.append(Format.floatFlex(distanceMatrix.get(0, 1))).append("\n");
    }

    private void summaryMore(StringBuilder sb, Printer printer, POption<?>... options) {
        sb.append(String.format("> %s[%s] - %s\n", corrName(), Arrays.deepToString(distanceMatrix.names()), corrDescription()));

        TextTable tt = TextTable.empty(distanceMatrix.names().length + 1, distanceMatrix.names().length + 1, 1, 1);
        tt.textCenter(0, 0, "");

        for (int i = 1; i < distanceMatrix.names().length + 1; i++) {
            tt.textCenter(0, i, i + "." + distanceMatrix.name(i - 1));
            tt.textLeft(i, 0, i + "." + distanceMatrix.name(i - 1));
            for (int j = 1; j < distanceMatrix.names().length + 1; j++) {
                double value = distanceMatrix.get(i - 1, j - 1);
                if (Double.isNaN(value)) {
                    tt.textCenter(i, j, "NaN");
                } else {
                    tt.floatFlex(i, j, value);
                }
            }
        }
        sb.append(tt.getDynamicText(printer, options));
    }
}
