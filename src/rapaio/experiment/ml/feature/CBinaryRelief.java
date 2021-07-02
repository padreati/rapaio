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

package rapaio.experiment.ml.feature;

import rapaio.core.RandomSource;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.experiment.ml.feature.relief.ReliefDifferenceFunction;
import rapaio.experiment.ml.feature.relief.ReliefDistanceFunction;
import rapaio.experiment.ml.feature.relief.ReliefImportance;
import rapaio.math.linear.DMatrix;
import rapaio.util.collection.IntArrays;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * ReliefF algorithm for feature selection.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/19/18.
 */
public class CBinaryRelief {

    private double p = 0.1;
    private int runs = 10;
    private final ReliefDistanceFunction distFun = ReliefDistanceFunction.l2();
    private final ReliefDifferenceFunction diffFun = ReliefDifferenceFunction.standard();
    private BiConsumer<CBinaryRelief, Integer> runningHook = null;

    private List<String> inputNames;
    private String targetName;
    private boolean[] numeric;
    private double[] weights;
    private boolean[] target;

    private DMatrix x;

    public static CBinaryRelief newReliefF() {
        return new CBinaryRelief();
    }

    private CBinaryRelief() {
    }

    public CBinaryRelief withRunningHook(BiConsumer<CBinaryRelief, Integer> hook) {
        this.runningHook = hook;
        return this;
    }

    public CBinaryRelief withP(double p) {
        this.p = p;
        return this;
    }

    public CBinaryRelief withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public ReliefImportance fit(Frame df, String targetName) {
        inputNames = Arrays.stream(df.varNames()).filter(name -> !targetName.equals(name)).collect(Collectors.toList());
        this.targetName = targetName;

        x = DMatrix.empty(df.rowCount(), inputNames.size());
        numeric = new boolean[inputNames.size()];

        for (int i = 0; i < inputNames.size(); i++) {
            String varName = inputNames.get(i);
            Var v = df.rvar(varName);
            VarType type = v.type();

            switch (type) {
                case DOUBLE, INT -> {
                    numeric[i] = true;
                    double min = Minimum.of(v).value();
                    double max = Maximum.of(v).value();
                    for (int j = 0; j < df.rowCount(); j++) {
                        if (v.isMissing(j)) {
                            x.set(j, i, Double.NaN);
                        } else {
                            x.set(j, i, (v.getDouble(j) + min) / (max - min));
                        }
                    }
                }
                case BINARY -> {
                    numeric[i] = true;
                    for (int j = 0; j < df.rowCount(); j++) {
                        if (v.isMissing(j)) {
                            x.set(j, i, Double.NaN);
                        } else {
                            x.set(j, i, v.getInt(j));
                        }
                    }
                }
                case NOMINAL -> {
                    numeric[i] = false;
                    for (int j = 0; j < df.rowCount(); j++) {
                        x.set(j, i, v.isMissing(j) ? Double.NaN : v.getInt(j));
                    }
                }
            }
        }

        target = new boolean[df.rowCount()];
        for (int i = 0; i < df.rowCount(); i++) {
            target[i] = df.getInt(i, targetName) == 1;
        }

        weights = new double[inputNames.size()];

        for (int run = 0; run < runs; run++) {

            int[] seq = new int[df.rowCount()];
            for (int i = 0; i < seq.length; i++) {
                seq[i] = i;
            }
            IntArrays.shuffle(seq, RandomSource.getRandom());

            int[] rows = new int[(int) (df.rowCount() * p)];
            int rlen = rows.length;
            int countTrue = 0;
            int countFalse = 0;
            int pos = 0;
            for (int i : seq) {
                if (pos == rows.length) {
                    break;
                }
                if (countTrue < rlen / 2 && target[i]) {
                    rows[pos] = i;
                    pos++;
                    countTrue++;
                    continue;
                }
                if (countFalse < (rlen - (rlen / 2)) && !target[i]) {
                    rows[pos] = i;
                    pos++;
                    countFalse++;
                }
            }

            // compute distance matrix within sample

            DMatrix dm = DMatrix.empty(rlen, rlen);
            for (int i = 0; i < rlen; i++) {
                for (int j = i + 1; j < rlen; j++) {
                    double dist = distFun.distance(x, numeric, rows[i], rows[j]);
                    dm.set(i, j, dist);
                    dm.set(j, i, dist);
                }
            }

            // find nearest hit and nearest miss for each row

            int[] hit = new int[rlen];
            int[] miss = new int[rlen];

            Arrays.fill(hit, -1);
            Arrays.fill(miss, -1);

            for (int row = 0; row < rlen; row++) {
                for (int col = 0; col < rlen; col++) {
                    if (row == col) {
                        continue;
                    }
                    if (target[rows[row]] == target[rows[col]]) {
                        // hit
                        if (hit[row] == -1 || dm.get(row, col) < dm.get(row, hit[row])) {
                            hit[row] = col;
                        }
                    } else {
                        // miss
                        if (miss[row] == -1 || dm.get(row, col) < dm.get(row, miss[row])) {
                            miss[row] = col;
                        }
                    }
                }
            }

            // update weights

            for (int row = 0; row < rlen; row++) {
                for (int i = 0; i < weights.length; i++) {
                    weights[i] -= diffFun.difference(x, numeric, i, rows[row], rows[hit[row]]) / rlen;
                    weights[i] += diffFun.difference(x, numeric, i, rows[row], rows[miss[row]]) / rlen;
                }
            }

            if (runningHook != null) {
                runningHook.accept(this, run);
            }
        }

        return new ReliefImportance(inputNames.toArray(new String[0]), weights);
    }

    public ReliefImportance getImportance() {
        return new ReliefImportance(inputNames.toArray(new String[0]), weights);
    }
}
