/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.supervised.stat;

import rapaio.core.BaseMath;
import rapaio.core.Summarizable;
import rapaio.data.Vector;
import rapaio.explore.Workspace;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConfusionMatrix implements Summarizable {

    private final Vector actual;
    private final Vector predict;
    private final String[] dict;
    private final int[][] cmf;
    private final double[][] cmp;

    public ConfusionMatrix(Vector actual, Vector predict) {
        validate(actual, predict);
        this.actual = actual;
        this.predict = predict;
        this.dict = actual.getDictionary();
        this.cmf = new int[dict.length][dict.length];
        this.cmp = new double[dict.length][dict.length];
        compute();
    }

    private void validate(Vector actual, Vector predict) {
        if (!actual.isNominal()) {
            throw new IllegalArgumentException("actual values vector must be nominal");
        }
        if (!predict.isNominal()) {
            throw new IllegalArgumentException("predict values vector must be nominal");
        }
        if (actual.getDictionary().length != predict.getDictionary().length) {
            throw new IllegalArgumentException("actual and predict does not have the same nominal dictionary");
        }
        for (int i = 0; i < actual.getDictionary().length; i++) {
            if (!actual.getDictionary()[i].equals(predict.getDictionary()[i])) {
                throw new IllegalArgumentException("actual and predict does not have the same nominal dictionary");
            }
        }
    }

    private final void compute() {
        for (int i = 0; i < actual.getRowCount(); i++) {
            cmf[actual.getIndex(i)][predict.getIndex(i)]++;
        }
    }

    @Override
    public void summary() {
        StringBuilder sb = new StringBuilder();

        addPreamble(sb);
        addConfusionMatrix(sb);
        addAccuracy(sb);

        Workspace.code(sb.toString());
    }

    private void addAccuracy(StringBuilder sb) {
        double acc = 0;
        for (int i = 0; i < actual.getRowCount(); i++) {
            if (actual.getIndex(i) == predict.getIndex(i)) {
                acc++;
            }
        }
        acc /= (1. * actual.getRowCount());
        sb.append(String.format("Accuracy: %.4f\n", acc));
    }

    private void addPreamble(StringBuilder sb) {
        sb.append("\nConfusion matrix summary statistics for " +
                "actual vector: ").append(actual.getName())
                .append(", predict vector: ").append(predict.getName()).append("\n");
        sb.append("======================\n\n");
    }

    private void addConfusionMatrix(StringBuilder sb) {
        sb.append("Confusion matrix\n\n");

        int maxwidth = 0;
        for (int i = 0; i < dict.length; i++) {
            maxwidth = BaseMath.max(maxwidth, dict[i].length());
            for (int j = 0; j < dict.length; j++) {
                maxwidth = BaseMath.max(maxwidth, String.format("%d", cmf[i][j]).length());
            }
        }

        sb.append("Predicted\n");
        for (int i = 0; i < dict.length; i++) {
            sb.append(String.format("%" + maxwidth + "s ", dict[i]));
        }
        sb.append("\n");

        for (int i = 0; i < dict.length; i++) {
            for (int j = 0; j < maxwidth; j++) {
                sb.append("-");
            }
            sb.append(" ");
        }
        sb.append("\n");

        for (int i = 0; i < dict.length; i++) {
            for (int j = 0; j < dict.length; j++) {
                sb.append(String.format("%" + maxwidth + "d ", cmf[i][j]));
            }
            sb.append(" |").append(String.format("%" + maxwidth + "s ", dict[i]));
            if (i == 1) {
                sb.append("   <- Actual");
            }
            sb.append("\n");
        }
    }
}
