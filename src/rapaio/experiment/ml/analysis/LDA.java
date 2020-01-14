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

package rapaio.experiment.ml.analysis;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.stream.FSpot;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.EigenPair;
import rapaio.math.linear.Linear;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;
import rapaio.printer.Printable;

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
    protected DVector eigenValues;
    protected DMatrix eigenVectors;

    protected String[] inputNames;
    protected DVector mean;
    protected DVector sd;

    protected boolean scaling = true;

    public DVector getEigenValues() {
        return eigenValues;
    }

    public DMatrix getEigenVectors() {
        return eigenVectors;
    }

    private String targetName;
    private List<String> targetLevels;

    private DVector[] classMean;

    public LDA withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public LDA withTol(double tol) {
        this.tol = tol;
        return this;
    }
    
    public void fit(Frame df, String... targetVars) {
    	validate(df, targetVars);

        logger.fine("start lda predict");
        DMatrix xx = SolidDMatrix.copy(df.removeVars(VRange.of(targetName)));

        // compute mean and sd

        mean = SolidDVector.zeros(xx.colCount());
        sd = SolidDVector.zeros(xx.colCount());
        for (int i = 0; i < xx.colCount(); i++) {
            mean.set(i, xx.mapCol(i).mean());
            sd.set(i, Math.sqrt(xx.mapCol(i).variance()));
        }

        // scale the whole data if it is the case

        if (scaling) {
            for (int i = 0; i < xx.rowCount(); i++) {
                for (int j = 0; j < xx.colCount(); j++) {
                    if (sd.get(j) != 0)
                        xx.set(i, j, (xx.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        // compute sliced data for each class

        DMatrix[] x = new DMatrix[targetLevels.size()];
        for (int i = 0; i < targetLevels.size(); i++) {
            int index = i;
            x[i] = xx.mapRows(df.stream()
                    .filter(s -> s.getLabel(targetName).equals(targetLevels.get(index)))
                    .mapToInt(FSpot::row)
                    .toArray());
        }

        // compute class means

        classMean = new DVector[targetLevels.size()];
        for (int i = 0; i < targetLevels.size(); i++) {
            classMean[i] = SolidDVector.zeros(x[i].colCount());
            for (int j = 0; j < x[i].colCount(); j++) {
                classMean[i].set(j, x[i].mapCol(j).mean());
            }
        }

        // build within scatter matrix

        DMatrix sw = SolidDMatrix.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.size(); i++) {
            sw.plus(x[i].scatter());
        }

        // build between-class scatter matrix

        DMatrix sb = SolidDMatrix.empty(inputNames.length, inputNames.length);
        for (int i = 0; i < targetLevels.size(); i++) {
            DMatrix cm = scaling ? classMean[i].asMatrix() : classMean[i].asMatrix().minus(mean.asMatrix());
            sb.plus(cm.dot(cm.t()).times(x[i].rowCount()));
        }

        // inverse sw
		DMatrix swi = QRDecomposition.from(sw).solve(SolidDMatrix.identity(inputNames.length));
//        RM swi = new CholeskyDecomposition(sw).solve(SolidRM.identity(inputNames.length));

        // use decomp of sbe
        DMatrix sbplus = Linear.pdPower(sb, 0.5, maxRuns, tol);
        DMatrix sbminus = Linear.pdPower(sb, -0.5, maxRuns, tol);

        EigenPair p = Linear.eigenDecomp(sbplus.dot(swi).dot(sbplus), maxRuns, tol);

        logger.fine("compute eigenvalues");
        eigenValues = p.getRV();
        eigenVectors = sbminus.dot(p.getRM());
//        eigenVectors = p.vectors();

        logger.fine("sort eigen values and vectors");

        Integer[] rows = new Integer[eigenValues.size()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        Arrays.sort(rows, (o1, o2) -> -Double.compare(eigenValues.get(o1), eigenValues.get(o2)));
        int[] indexes = Arrays.stream(rows).mapToInt(v -> v).toArray();

        eigenValues = eigenValues.asMatrix().mapRows(indexes).mapCol(0).copy();
        eigenVectors = eigenVectors.mapCols(indexes).copy();
    }

    public Frame predict(Frame df, BiFunction<DVector, DMatrix, Integer> kFunction) {
        DMatrix x = SolidDMatrix.copy(df.mapVars(inputNames));

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
        DMatrix result = x.dot(eigenVectors.mapCols(dim));
        Frame rest = df.removeVars(VRange.of(inputNames));
        return rest.varCount() == 0 ?
                SolidFrame.matrix(result, names) :
                SolidFrame.matrix(result, names).bindVars(df.removeVars(VRange.of(inputNames)));
    }

    private void validate(Frame df, String... targetVars) {

        List<String> targetNames = VRange.of(targetVars).parseVarNames(df);
        if (targetNames.isEmpty() || targetNames.size() > 1)
            throw new IllegalArgumentException("LDA needs one target var");
        targetName = targetNames.get(0);

        Set<VType> allowedTypes = new HashSet<>(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE));
        df.varStream().forEach(var -> {
            if (targetName.equals(var.name())) {
                if (!var.type().equals(VType.NOMINAL)) {
                    throw new IllegalArgumentException("target var must be nominal");
                }
                List<String> varLevels = var.levels();
                targetLevels = var.levels().subList(1, varLevels.size());
                return;
            }
            if (!allowedTypes.contains(var.type())) {
                throw new IllegalArgumentException("column type not allowed");
            }
        });
        inputNames = df.varStream().filter(v -> !v.name().equals(targetName)).map(Var::name).toArray(String[]::new);
    }

    @Override
	public String toSummary() {
        StringBuilder sb = new StringBuilder();

        Frame eval = SolidFrame.byVars(
                VarDouble.empty(eigenValues.size()).withName("values"),
                VarDouble.empty(eigenValues.size()).withName("percent")
        );
        double total = 0.0;
        for (int i = 0; i < eigenValues.size(); i++) {
            total += eigenValues.get(i);
        }
        for (int i = 0; i < eigenValues.size(); i++) {
            eval.setDouble(i, "values", eigenValues.get(i));
            eval.setDouble(i, "percent", eigenValues.get(i) / total);
        }

        sb.append("Eigen values\n");
        sb.append("============\n");
        sb.append(eval.toFullContent()).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.toSummary()).append("\n");

        return sb.toString();
    }
}
