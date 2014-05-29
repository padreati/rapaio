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

package rapaio.core.stat;

import rapaio.core.Printable;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RMSE implements Printable {

    private final List<Var> source;
    private final List<Var> target;
    private double value;

    public RMSE(Frame dfSource, Frame dfTarget) {
        source = new ArrayList<>();
        for (int i = 0; i < dfSource.colCount(); i++) {
            if (dfSource.col(i).type().isNumeric()) {
                source.add(dfSource.col(i));
            }
        }
        target = new ArrayList<>();
        for (int i = 0; i < dfTarget.colCount(); i++) {
            if (dfTarget.col(i).type().isNumeric()) {
                target.add(dfTarget.col(i));
            }
        }
        compute();
    }

    public RMSE(Var source, Var target) {
        this.source = new ArrayList<>();
        this.source.add(source);
        this.target = new ArrayList<>();
        this.target.add(target);
        compute();
    }

    private void compute() {
        double total = 0;
        double count = 0;

        for (int i = 0; i < source.size(); i++) {
            for (int j = 0; j < source.get(i).rowCount(); j++) {
                count++;
                total += Math.pow(source.get(i).value(j) - target.get(i).value(j), 2);
            }
        }
        value = Math.sqrt(total / count);
    }

    public double getValue() {
        return value;
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> not implemented\n");
    }
}
