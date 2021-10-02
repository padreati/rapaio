/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import java.io.Serial;
import java.util.List;

import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;
import rapaio.sys.WS;

/**
 * Base abstract class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractFrame implements Frame {

    @Serial
    private static final long serialVersionUID = -4375603852723666661L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append("(");
        sb.append("rowCount=").append(rowCount()).append(", ");
        sb.append("varCount=").append(varCount()).append(")\n");
        int maxLen = 20;
        int lastLen = 4;
        List<Var> varList = varList();
        if (varCount() <= maxLen) {
            for (Var v : varList) {
                sb.append(v.toString()).append("\n");
            }
        } else {
            for (int i = 0; i < maxLen - lastLen; i++) {
                sb.append(varList.get(i).toString()).append("\n");
            }
            sb.append("....\n");
            for (int i = varCount() - lastLen; i < varCount(); i++) {
                sb.append(varList.get(i).toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return selection(10, 5, printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return selection(rowCount(), 0, printer, options);
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        StringBuilder sb = new StringBuilder();

        sb.append("Frame Summary\n");
        sb.append("=============\n");

        sb.append("* rowCount: ").append(rowCount()).append("\n");
        sb.append("* complete: ").append(stream().complete().count()).append("/").append(rowCount()).append("\n");
        sb.append("* varCount: ").append(varCount()).append("\n");
        sb.append("* varNames: \n");

        TextTable tt = TextTable.empty(varCount(), 5);
        for (int i = 0; i < varCount(); i++) {
            tt.textRight(i, 0, i + ".");
            tt.textRight(i, 1, rvar(i).name());
            tt.textLeft(i, 2, ":");
            tt.textLeft(i, 3, rvar(i).type().code());
            tt.textRight(i, 4, "|");
        }
        sb.append("\n").append(tt.getDynamicText(printer, options)).append("\n");

        sb.append("* summary: \n");

        tt = TextTable.empty(8, 2 * varCount());

        for (int i = 0; i < varCount(); i++) {
            tt.textRight(0, i * 2, " " + rvar(i).name());
            tt.textLeft(0, i * 2 + 1, "[" + rvar(i).type().code() + "]");

            if (rvar(i) instanceof AbstractVar) {

                ((AbstractVar) rvar(i)).fillSummary(tt, i * 2, i * 2 + 1);
            }
        }
        sb.append(tt.getDynamicText(printer, options)).append("\n");
        return sb.toString();
    }

    public String head() {
        return head(10);
    }

    public String head(int lines) {
        return selection(lines, 0, WS.getPrinter());
    }

    public void printHead() {
        WS.println(head());
    }

    public void printHead(int lines) {
        WS.println(head(lines));
    }

    private String selection(int head, int tail, Printer printer, POption<?>... options) {
        if (varCount() == 0) {
            return "";
        }
        Var[] vars = new Var[varCount()];
        String[] names = varNames();
        for (int i = 0; i < vars.length; i++) {
            vars[i] = rvar(i);
        }
        if (head == -1 || head >= rowCount()) {
            head = rowCount();
            tail = 0;
        }

        int rowCount = rowCount();
        int dots = (head + tail < rowCount) ? 1 : 0;

        TextTable tt = TextTable.empty(head + tail + dots + 1, vars.length + 1, 1, 1);
        for (int i = 0; i < vars.length; i++) {
            tt.textCenter(0, i + 1, names[i]);
        }
        for (int i = 0; i < head; i++) {
            tt.intRow(i + 1, 0, i);
        }
        for (int i = head + dots; i < head + dots + tail; i++) {
            tt.intRow(i + 1, 0, rowCount - tail - dots - head + i);
        }

        for (int i = 0; i < head; i++) {
            for (int j = 0; j < vars.length; j++) {
                tt.textType(i + 1, j + 1, vars[j], i);
            }
        }

        if (tail != 0) {
            for (int i = head; i < head + dots; i++) {
                for (int j = 0; j < vars.length; j++) {
                    tt.textCenter(i + 1, j + 1, "...");
                }
            }
            for (int i = head + dots; i < head + dots + tail; i++) {
                for (int j = 0; j < vars.length; j++) {
                    tt.textType(i + 1, j + 1, vars[j], rowCount - tail - dots - head + i);
                }
            }
        }
        return tt.getDynamicText(printer, options);
    }
}