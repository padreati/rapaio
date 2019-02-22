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

package rapaio.data;

import rapaio.printer.*;
import rapaio.printer.format.*;
import rapaio.sys.*;

import java.util.List;

/**
 * Base abstract class for a frame, which provides behavior for the utility
 * access methods based on row and column indexes.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
abstract class AbstractFrame implements Frame {

    private static final long serialVersionUID = -4375603852723666661L;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getName()).append("(");
        sb.append("rowCount=").append(rowCount()).append(", ");
        sb.append("varCount=").append(varCount()).append(")\n");
        int max_len = 20;
        int last_len = 4;
        List<Var> varList = varList();
        if (varCount() <= max_len) {
            for (Var v : varList) {
                sb.append(v.toString()).append("\n");
            }
        } else {
            for (int i = 0; i < max_len - last_len; i++) {
                sb.append(varList.get(i).toString()).append("\n");
            }
            sb.append("....\n");
            for (int i = varCount() - last_len; i < varCount(); i++) {
                sb.append(varList.get(i).toString());
            }
        }
        return sb.toString();
    }

    @Override
    public String content() {
        return selection(10, 5);
    }

    @Override
    public String fullContent() {
        return selection(rowCount(), 0);
    }

    @Override
    public String summary() {
        return Summary.getSummary(this);
    }

    public String head() {
        return head(10);
    }

    public String head(int lines) {
        return selection(lines, 0);
    }

    public void printHead() {
        WS.println(head());
    }

    public void printHead(int lines) {
        WS.println(head(lines));
    }

    private String selection(int head, int tail) {
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
        return tt.getDefaultText();
    }

}