/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.EigenPair;
import rapaio.math.linear.Linear;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
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

    public RV getEigenValues() {
        return eigenValues;
    }

    public RM getEigenVectors() {
        return eigenVectors;
    }

    public void learn(Frame df, String... targetVars) {
        validate(df, targetVars);

        logger.fine("start lda train");
        RM xx = SolidRM.copy(df.removeVars(targetName));

        // compute mean and sd

        mean = SolidRV.empty(xx.getColCount());
        sd = SolidRV.empty(xx.getColCount());
        for (int i = 0; i < xx.getColCount(); i++) {
            mean.set(i, xx.mapCol(i).mean().getValue());
            sd.set(i, xx.mapCol(i).variance().sdValue());
        }

        // scale the whole data if it is the case

        if (scaling) {
            for (int i = 0; i < xx.getRowCount(); i++) {
                for (int j = 0; j < xx.getColCount(); j++) {
                    if (sd.get(j) != 0)
                        xx.set(i, j, (xx.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        // compute sliced data for each class

        RM[] x = new RM[targetLevels.length];
        for (int i = 0; i < targetLevels.length; i++) {
            int index = i;
            x[i] = xx.mapRows(df.stream()
                    .filter(s -> s.getLabel(targetName).equals(targetLevels[index]))
                    .mapToInt(FSpot::getRow)
                    .toArray());
        }

        // compute class means

        classMean = new RV[targetLevels.length];
        for (int i = 0; i < targetLevels.length; i++) {
            classMean[i] = SolidRV.empty(x[i].getColCount());
            for (int j = 0; j < x[i].getColCount(); j++) {
                classMean[i].set(j, x[i].mapCol(j).mean().getValue());
            }
        }

        // build within scatter matrix

        RM sw = SolidRM.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.length; i++) {
            sw.plus(x[i].scatter());
        }

        // build between-class scatter matrix

        RM sb = SolidRM.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.length; i++) {
            RM cm = scaling ? classMean[i].asMatrix() : classMean[i].asMatrix().minus(mean.asMatrix());
            sb.plus(cm.dot(cm.t()).dot(x[i].getRowCount()));
        }

        // inverse sw
		RM swi = QRDecomposition.from(sw).solve(SolidRM.identity(inputNames.length));
//        RM swi = new CholeskyDecomposition(sw).solve(SolidRM.identity(inputNames.length));

        // use decomp of sbe
        RM sbplus = Linear.pdPower(sb, 0.5, maxRuns, tol);
        RM sbminus = Linear.pdPower(sb, -0.5, maxRuns, tol);

        EigenPair p = Linear.eigenDecomp(sbplus.dot(swi).dot(sbplus), maxRuns, tol);

        logger.fine("compute eigenvalues");
        eigenValues = p.getRV();
        eigenVectors = sbminus.dot(p.getRM());
//        eigenVectors = p.vectors();

        logger.fine("sort eigen values and vectors");

        Integer[] rows = new Integer[eigenValues.count()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        Arrays.sort(rows, (o1, o2) -> -Double.valueOf(eigenValues.get(o1)).compareTo(eigenValues.get(o2)));
        int[] indexes = Arrays.stream(rows).mapToInt(v -> v).toArray();

        eigenValues = eigenValues.asMatrix().mapRows(indexes).mapCol(0).solidCopy();
        eigenVectors = eigenVectors.mapCols(indexes).solidCopy();
    }


    public Frame fit(Frame df, BiFunction<RV, RM, Integer> kFunction) {
        RM x = SolidRM.copy(df.mapVars(inputNames));

        if (scaling) {
            for (int i = 0; i < x.getRowCount(); i++) {
                for (int j = 0; j < x.getColCount(); j++) {
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
        Frame rest = df.removeVars(inputNames);
        return rest.getVarCount() == 0 ?
                SolidFrame.matrix(result, names) :
                SolidFrame.matrix(result, names).bindVars(df.removeVars(inputNames));
    }

    private void validate(Frame df, String... targetVars) {

        List<String> targetNames = VRange.of(targetVars).parseVarNames(df);
        if (targetNames.isEmpty() || targetNames.size() > 1)
            throw new IllegalArgumentException("LDA needs one target var");
        targetName = targetNames.get(0);

        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INDEX, VarType.ORDINAL, VarType.NUMERIC));
        df.varStream().forEach(var -> {
            if (targetName.equals(var.getName())) {
                if (!var.getType().equals(VarType.NOMINAL)) {
                    throw new IllegalArgumentException("target var must be nominal");
                }
                targetLevels = new String[var.getLevels().length - 1];
                System.arraycopy(var.getLevels(), 1, targetLevels, 0, var.getLevels().length - 1);
                return;
            }
            if (!allowedTypes.contains(var.getType())) {
                throw new IllegalArgumentException("column type not allowed");
            }
        });
        inputNames = df.varStream().filter(v -> !v.getName().equals(targetName)).map(Var::getName).toArray(String[]::new);
    }

    @Override
	public String getSummary() {
        StringBuilder sb = new StringBuilder();

        Frame eval = SolidFrame.byVars(
                NumericVar.empty(eigenValues.count()).withName("values"),
                NumericVar.empty(eigenValues.count()).withName("percent")
        );
        double total = 0.0;
        for (int i = 0; i < eigenValues.count(); i++) {
            total += eigenValues.get(i);
        }
        for (int i = 0; i < eigenValues.count(); i++) {
            eval.setValue(i, "values", eigenValues.get(i));
            eval.setValue(i, "percent", eigenValues.get(i) / total);
        }

        sb.append("Eigen values\n");
        sb.append("============\n");
        sb.append(Summary.headString(true, eval)).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.getSummary()).append("\n");

        return sb.toString();
    }
}
