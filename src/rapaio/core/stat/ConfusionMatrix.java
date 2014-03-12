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

import rapaio.core.MathBase;
import rapaio.core.Summarizable;
import rapaio.data.Vector;
import rapaio.workspace.Workspace;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConfusionMatrix implements Summarizable {

    private final Vector actual;
    private final Vector predict;
    private final String[] dict;
    private final int[][] cmf;
    private final boolean percents;
    private double acc;
    private double completeCases = 0;

    public ConfusionMatrix(Vector actual, Vector predict) {
        this(actual, predict, false);
    }

    public ConfusionMatrix(Vector actual, Vector predict, boolean percents) {
        validate(actual, predict);
        this.actual = actual;
        this.predict = predict;
        this.dict = actual.getDictionary();
        this.cmf = new int[dict.length - 1][dict.length - 1];
        this.percents = percents;
        compute();
    }

    private void validate(Vector actual, Vector predict) {
        if (!actual.getType().isNominal()) {
            throw new IllegalArgumentException("actual values vector must be nominal");
        }
        if (!predict.getType().isNominal()) {
            throw new IllegalArgumentException("predict values vector must be nominal");
        }
        if (actual.getDictionary().length != predict.getDictionary().length) {
            throw new IllegalArgumentException("actual and predict does not have the same nominal getDictionary");
        }
        for (int i = 0; i < actual.getDictionary().length; i++) {
            if (!actual.getDictionary()[i].equals(predict.getDictionary()[i])) {
                throw new IllegalArgumentException("actual and predict does not have the same nominal getDictionary");
            }
        }
    }

    private void compute() {
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getIndex(i) != 0 && predict.getIndex(i) != 0) {
                completeCases++;
                cmf[actual.getIndex(i) - 1][predict.getIndex(i) - 1]++;
            }
        }
        acc = 0;
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getIndex(i) == predict.getIndex(i) && actual.getIndex(i) != 0) {
                acc++;
            }
        }

        if (completeCases == 0) {
            acc = 0;
        } else {
            acc = acc / completeCases;
        }
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();

        addConfusionMatrix(sb);
        addDetails(sb);

        Workspace.code(sb.toString());
    }

    private void addDetails(StringBuilder sb) {
        sb.append(String.format("\nComplete cases %d from %d\n", (int) Math.rint(completeCases), actual.getRowCount()));
        sb.append(String.format("Accuracy: %.4f\n", acc));
    }

    private void addConfusionMatrix(StringBuilder sb) {
        sb.append("Confusion rapaio.data.matrix\n");

        sb.append("\n");
        int maxwidth = "Actual".length();
        for (int i = 1; i < dict.length; i++) {
            maxwidth = MathBase.max(maxwidth, dict[i].length());
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                maxwidth = MathBase.max(maxwidth, String.format("%d", cmf[i - 1][j - 1]).length());
                total += cmf[i - 1][j - 1];
            }
            maxwidth = MathBase.max(maxwidth, String.format("%d", total).length());
        }

        sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n");
        sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|");
        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i]));
            if (i != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "s ", "Total"));
        sb.append("\n");
        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");

        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i])).append("|");
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                sb.append(String.format("%" + maxwidth + "d", cmf[i - 1][j - 1]));
                if (j != dict.length - 1) {
                    sb.append(" ");
                } else {
                    sb.append("|");
                }
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format("%" + maxwidth + "d", total));
            sb.append("\n");
        }

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");


        sb.append(String.format("%" + maxwidth + "s", "Total")).append("|");
        for (int j = 1; j < dict.length; j++) {
            int total = 0;
            for (int i = 1; i < dict.length; i++) {
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format("%" + maxwidth + "d", total));
            if (j != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "d", (int) Math.rint(completeCases)));
        sb.append("\n");


        // percents

        if (!percents || completeCases == 0.) return;

        sb.append("\n");
        maxwidth = "Actual".length();
        for (int i = 1; i < dict.length; i++) {
            maxwidth = MathBase.max(maxwidth, dict[i].length());
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                maxwidth = MathBase.max(maxwidth, String.format("%.3f", cmf[i - 1][j - 1] / completeCases).length());
                total += cmf[i - 1][j - 1];
            }
            maxwidth = MathBase.max(maxwidth, String.format("%.3f", total / completeCases).length());
        }

        sb.append(String.format("%" + maxwidth + "s", "")).append("|").append(" Predicted\n");
        sb.append(String.format("%" + maxwidth + "s", "Actual")).append("|");
        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i]));
            if (i != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format("%" + maxwidth + "s ", "Total"));
        sb.append("\n");

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");

        for (int i = 1; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s", dict[i])).append("|");
            int total = 0;
            for (int j = 1; j < dict.length; j++) {
                sb.append(String.format(" %.3f", cmf[i - 1][j - 1] / completeCases));
                if (j != dict.length - 1) {
                    sb.append(" ");
                } else {
                    sb.append("|");
                }
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format(" %.3f", total / completeCases));
            sb.append("\n");
        }

        for (int i = 1; i < dict.length + 1; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        for (int j = 0; j < maxwidth; j++) {
            sb.append("-");
        }
        sb.append(" ");
        sb.append("\n");


        sb.append(String.format("%" + maxwidth + "s", "Total")).append("|");
        for (int j = 1; j < dict.length; j++) {
            int total = 0;
            for (int i = 1; i < dict.length; i++) {
                total += cmf[i - 1][j - 1];
            }
            sb.append(String.format(" %.3f", total / completeCases));
            if (j != dict.length - 1) {
                sb.append(" ");
            } else {
                sb.append("|");
            }
        }
        sb.append(String.format(" %.3f", 1.));
        sb.append("\n");

    }

    public double getAccuracy() {
        return acc;
    }

    public int getCompleteCases() {
        return (int) Math.rint(completeCases);
    }

    public void setCompleteCases(double completeCases) {
        this.completeCases = completeCases;
    }

    public int[][] getMatrix() {
        return cmf;
    }
}
