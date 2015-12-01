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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarType;
import rapaio.math.linear.EigenPair;
import rapaio.math.linear.Linear;
import rapaio.math.linear.RM;
import rapaio.math.linear.RV;
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

    private RV mean;
    private RV sd;

    private RM eigenValues;
    private RM eigenVectors;

    public PCA withMaxRuns(int maxRuns) {
        this.maxRuns = maxRuns;
        return this;
    }

    public PCA withTol(double tol) {
        this.tol = tol;
        return this;
    }

    public RM getEigenValues() {
        return eigenValues;
    }

    public RM getEigenVectors() {
        return eigenVectors;
    }

    public void learn(Frame df) {
        validate(df);

        logger.fine("start pca train");
        RM x = Linear.newRMCopyOf(df);
        if (scaling) {
            logger.fine("compute mean, sd and do scaling");
            mean = RV.empty(x.colCount());
            sd = RV.empty(x.colCount());
            for (int i = 0; i < x.colCount(); i++) {
                mean.set(i, x.mapCol(i).mean().value());
                sd.set(i, x.mapCol(i).var().sdValue());
            }
            for (int i = 0; i < x.rowCount(); i++) {
                for (int j = 0; j < x.colCount(); j++) {
                    x.set(i, j, (x.get(i, j) - mean.get(j)) / sd.get(j));
                }
            }
        }

        logger.fine("build scatter");
        RM s = x.scatter();

        logger.fine("compute eigenvalues");
        EigenPair ep = Linear.pdEigenDecomp(s, maxRuns, tol);
        eigenValues = ep.values();
        eigenVectors = ep.vectors();

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

    public Frame fit(Frame df, int k) {
        // TODO check if we have all the initial columns

        RM x = Linear.newRMCopyOf(df);

        if (scaling) {
            for (int i = 0; i < x.rowCount(); i++) {
                for (int j = 0; j < x.colCount(); j++) {
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
        return SolidFrame.newMatrix(result, names);
    }

    private void validate(Frame df) {
        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INDEX, VarType.ORDINAL, VarType.NUMERIC));
        df.varStream().forEach(var -> {
            if (!allowedTypes.contains(var.type())) {
                throw new IllegalArgumentException("column type not allowed");
            }
        });
    }

    public String summary() {
        StringBuilder sb = new StringBuilder();

        sb.append("Eigen values\n");
        sb.append("============\n");
        sb.append(eigenValues.summary()).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.summary()).append("\n");

        return sb.toString();
    }
}
