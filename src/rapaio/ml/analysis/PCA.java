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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.math.linear.EigenPair;
import rapaio.math.linear.Linear;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;
import rapaio.printer.Printable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
 */
public class PCA implements Printable {

    private static final Logger logger = Logger.getLogger(PCA.class.getName());

    private double tol = 1e-10;
    private int maxRuns = 2_000;

    private boolean scaling = true;
    private String[] inputNames;

    private RV mean;
    private RV sd;

    private RV eigenValues;
    private RM eigenVectors;

    public PCA withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public PCA withTol(double tol) {
        this.tol = tol;
        return this;
    }

    public PCA withScaling(boolean scaling) {
        this.scaling = scaling;
        return this;
    }

    public RV getEigenValues() {
        return eigenValues;
    }

    public RM getEigenVectors() {
        return eigenVectors;
    }

    public void train(Frame df) {
        validate(df);

        logger.fine("start pca train");
        RM x = SolidRM.copy(df);
        if (scaling) {
            logger.fine("compute mean, sd and do scaling");
            mean = SolidRV.empty(x.getColCount());
            sd = SolidRV.empty(x.getColCount());
            for (int i = 0; i < x.getColCount(); i++) {
                mean.set(i, x.mapCol(i).mean().getValue());
                sd.set(i, x.mapCol(i).variance().sdValue());
            }
            for (int i = 0; i < x.getRowCount(); i++) {
                for (int j = 0; j < x.getColCount(); j++) {
                    if (sd.get(j) == 0)
                        continue;
                    x.set(i, j, (x.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        logger.fine("build scatter");
        RM s = x.scatter();

        logger.fine("compute eigenvalues");
        EigenPair ep = Linear.eigenDecomp(s, maxRuns, tol);
        eigenValues = ep.getRV();
        eigenVectors = ep.getRM();

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

    public Frame fit(Frame df, int k) {
        // TODO check if we have all the initial columns

        RM x = SolidRM.copy(df.mapVars(inputNames));

        if (scaling) {
            for (int i = 0; i < x.getRowCount(); i++) {
                for (int j = 0; j < x.getColCount(); j++) {
                    if (sd.get(j) != 0)
                        x.set(i, j, (x.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        int[] dim = new int[k];
        String[] names = new String[k];
        for (int i = 0; i < dim.length; i++) {
            dim[i] = i;
            names[i] = "pca_" + (i + 1);
        }
        RM result = x.dot(eigenVectors.mapCols(dim));
        Frame rest = df.removeVars(inputNames);
        return rest.getVarCount() == 0 ?
                SolidFrame.matrix(result, names) :
                SolidFrame.matrix(result, names).bindVars(rest);
    }

    private void validate(Frame df) {
        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INDEX, VarType.ORDINAL, VarType.NUMERIC));
        df.varStream().forEach(var -> {
            if (!allowedTypes.contains(var.getType())) {
                throw new IllegalArgumentException("Var type not allowed. Var name: " + var.getName() + ", type: " + var.getType().name());
            }
        });

        inputNames = df.varStream().map(Var::getName).toArray(String[]::new);
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();

        sb.append("Eigen values\n");
        sb.append("============\n");
        sb.append(eigenValues.getSummary()).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.getSummary()).append("\n");

        return sb.toString();
    }
}
