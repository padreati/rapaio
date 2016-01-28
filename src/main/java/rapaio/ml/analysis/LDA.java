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

package rapaio.ml.analysis;

import rapaio.data.*;
import rapaio.data.stream.FSpot;
import rapaio.math.linear.*;
import rapaio.printer.Printable;
import rapaio.printer.Summary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * Linear discriminant analysis
 * <p>
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/5/15.
 */
public class LDA implements Printable {
    private static final Logger logger = Logger.getLogger(LDA.class.getName());

    private double tol = 1e-24;
    private int maxRuns = 10_000;

    private boolean scaling = true;

    private String targetName;
    private String[] targetLevels;
    private String[] inputNames;
    private RV mean;
    private RV sd;

    private RV[] classMean;

    private RV eigenValues;
    private RM eigenVectors;

    public LDA withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public LDA withTol(double tol) {
        this.tol = tol;
        return this;
    }

    public RM getEigenValues() {
        return eigenValues;
    }

    public RM getEigenVectors() {
        return eigenVectors;
    }

    public void learn(Frame df, String... targetVars) {
        validate(df, targetVars);

        logger.fine("start lda train");
        RM xx = Linear.newRMCopyOf(df.removeVars(targetName));

        // compute mean and sd

        mean = RV.empty(xx.colCount());
        sd = RV.empty(xx.colCount());
        for (int i = 0; i < xx.colCount(); i++) {
            mean.set(i, xx.mapCol(i).mean().value());
            sd.set(i, xx.mapCol(i).var().sdValue());
        }

        // scale the whole data if it is the case

        if (scaling) {
            for (int i = 0; i < xx.rowCount(); i++) {
                for (int j = 0; j < xx.colCount(); j++) {
                    xx.set(i, j, (xx.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        // compute sliced data for each class

        RM[] x = new RM[targetLevels.length];
        for (int i = 0; i < targetLevels.length; i++) {
            int index = i;
            x[i] = xx.mapRows(df.stream()
                    .filter(s -> s.label(targetName).equals(targetLevels[index]))
                    .mapToInt(FSpot::row)
                    .toArray());
        }

        // compute class means

        classMean = new RV[targetLevels.length];
        for (int i = 0; i < targetLevels.length; i++) {
            classMean[i] = RV.empty(x[i].colCount());
            for (int j = 0; j < x[i].colCount(); j++) {
                classMean[i].set(j, x[i].mapCol(j).mean().value());
            }
        }

        // build within scatter matrix

        RM sw = RM.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.length; i++) {
            sw.plus(x[i].scatter());
        }

        // build between-class scatter matrix

        RM sb = RM.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.length; i++) {
            RM cm = scaling ? classMean[i].copy() : classMean[i].copy().minus(mean);
            sb.plus(cm.dot(cm.t()).dot(x[i].rowCount()));
        }

        // inverse sw
        RM swi = new CholeskyDecomposition(sw).solve(RM.identity(inputNames.length));

        // use decomp of sbe
        RM sbplus = Linear.pdPower(sb, 0.5, maxRuns, tol);
        RM sbminus = Linear.pdPower(sb, -0.5, maxRuns, tol);

        EigenPair p = Linear.pdEigenDecomp(sbplus.dot(swi).dot(sbplus), maxRuns, tol);

        logger.fine("compute eigenvalues");
        eigenValues = p.values().mapCol(0);
        eigenVectors = sbminus.dot(p.vectors());
//        eigenVectors = p.vectors();

        logger.fine("sort eigen values and vectors");

        Integer[] rows = new Integer[eigenValues.rowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        Arrays.sort(rows, (o1, o2) -> -Double.valueOf(eigenValues.get(o1, 0)).compareTo(eigenValues.get(o2, 0)));
        int[] indexes = Arrays.stream(rows).mapToInt(v -> v).toArray();

        eigenValues = eigenValues.mapRows(indexes).mapCol(0).copy();
        eigenVectors = eigenVectors.mapCols(indexes).copy();
    }


    public Frame fit(Frame df, BiFunction<RV, RM, Integer> kFunction) {
        // TODO check if we have all the initial columns

        RM x = Linear.newRMCopyOf(df);

        if (scaling) {
            for (int i = 0; i < x.rowCount(); i++) {
                for (int j = 0; j < x.colCount(); j++) {
                    x.set(i, j, (x.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        int k = kFunction.apply(eigenValues, eigenVectors);

        int[] dim = new int[k];
        String[] names = new String[k];
        for (int i = 0; i < dim.length; i++) {
            dim[i] = i;
            names[i] = "lda_" + (i + 1);
        }
        RM result = x.dot(eigenVectors.mapCols(dim));
        return SolidFrame.matrix(result, names);
    }

    private void validate(Frame df, String... targetVars) {

        List<String> targetNames = VRange.of(targetVars).parseVarNames(df);
        if (targetNames.isEmpty() || targetNames.size() > 1)
            throw new IllegalArgumentException("LDA needs one target var");
        targetName = targetNames.get(0);

        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INDEX, VarType.ORDINAL, VarType.NUMERIC));
        df.varStream().forEach(var -> {
            if (targetName.equals(var.name())) {
                if (!var.type().equals(VarType.NOMINAL)) {
                    throw new IllegalArgumentException("target var must be nominal");
                }
                targetLevels = new String[var.levels().length - 1];
                System.arraycopy(var.levels(), 1, targetLevels, 0, var.levels().length - 1);
                return;
            }
            if (!allowedTypes.contains(var.type())) {
                throw new IllegalArgumentException("column type not allowed");
            }
        });
        inputNames = df.varStream().filter(v -> !v.name().equals(targetName)).map(Var::name).toArray(String[]::new);
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();

        Frame eval = SolidFrame.wrapOf(
                Numeric.empty(eigenValues.rowCount()).withName("values"),
                Numeric.empty(eigenValues.rowCount()).withName("percent")
        );
        double total = 0.0;
        for (int i = 0; i < eigenValues.rowCount(); i++) {
            total += eigenValues.get(i);
        }
        for (int i = 0; i < eigenValues.rowCount(); i++) {
            eval.setValue(i, "values", eigenValues.get(i));
            eval.setValue(i, "percent", eigenValues.get(i) / total);
        }

        sb.append("Eigen values\n");
        sb.append("============\n");
        sb.append(Summary.headString(true, eval)).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.summary()).append("\n");

        return sb.toString();
    }
}
