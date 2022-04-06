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

package rapaio.ml.analysis;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
 */
public class PCA extends ParamSet<PCA> implements Printable {

    public static PCA newModel() {
        return new PCA();
    }

    @Serial
    private static final long serialVersionUID = 6441166473576114983L;
    private static final Logger logger = Logger.getLogger(PCA.class.getName());

    /**
     * Value used to assess the convergence of a solution.
     */
    public final ValueParam<Double, PCA> eps = new ValueParam<>(this, 1e-100, "eps");

    /**
     * Maximum number of iterations for fitting procedure.
     */
    public final ValueParam<Integer, PCA> maxRuns = new ValueParam<>(this, 2_000, "maxRuns", m -> m != null && m > 0);

    /**
     * Flag which specifies if input scaling is applied.
     */
    public final ValueParam<Boolean, PCA> center = new ValueParam<>(this, true, "scaling");

    /**
     * Divide input by computed sample standard deviation.
     */
    public final ValueParam<Boolean, PCA> standardize = new ValueParam<>(this, false, "standardize");

    private int inputRows;
    private int inputVars;
    private String[] inputNames;

    private PCA() {
    }

    protected DVector eigenValues;
    protected DMatrix eigenVectors;
    protected DVector mean;
    protected DVector sd;

    public DVector getValues() {
        return eigenValues;
    }

    public DMatrix getVectors() {
        return eigenVectors;
    }

    public DVector getMean() {
        return mean;
    }

    public DVector getSd() {
        return sd;
    }

    public PCA fit(Frame df) {
        preFit(df);

        logger.fine("start pca predict");
        DMatrix x = DMatrix.copy(df);
        logger.fine("compute mean, sd and do scaling");
        if (center.get()) {
            mean = x.mean(0);
            x.sub(mean, 0);
        }
        if (standardize.get()) {
            sd = x.sd(0);
            x.div(sd, 0);
        }

        logger.fine("build scatter");
        DMatrix s = x.scatter();

        logger.fine("compute eigenvalues");
        var evd = s.evd();
        eigenValues = evd.real().div(x.rows() - 1);
        eigenVectors = evd.v();

        logger.fine("sort eigen values and vectors");

        int[] mapping = IntArrays.newSeq(0, eigenValues.size());
        DoubleArrays.quickSortIndirect(mapping, eigenValues.valueStream().toArray(), 0, eigenValues.size());
        IntArrays.reverse(mapping);

        eigenValues = eigenValues.asMatrix().mapRows(mapping).mapCol(0).copy();
        eigenVectors = eigenVectors.mapCols(mapping).copy();
        return this;
    }

    private void preFit(Frame df) {
        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INT, VarType.DOUBLE));
        df.varStream().forEach(var -> {
            if (!allowedTypes.contains(var.type())) {
                throw new IllegalArgumentException("Var type not allowed. Var name: " + var.name() + ", type: " + var.type().name());
            }
        });
        inputRows = df.rowCount();
        inputVars = df.varCount();
        inputNames = df.varStream().map(Var::name).toArray(String[]::new);
    }

    /**
     * Transforms a given matrix into projections of the first k principal components.
     *
     * @param df initial data frame
     * @param k  number of principal components used
     * @return transformed input
     */
    public Frame transform(Frame df, int k) {

        DMatrix x = DMatrix.copy(df.mapVars(inputNames));

        if (center.get()) {
            x.sub(mean, 0);
        }
        if (standardize.get()) {
            x.div(sd, 0);
        }

        String[] names = new String[k];
        for (int i = 0; i < k; i++) {
            names[i] = "pca_" + (i + 1);
        }

        DMatrix result = x.dot(eigenVectors.rangeCols(0, k));

        Frame rest = df.removeVars(VarRange.of(inputNames));
        Frame prediction = SolidFrame.matrix(result, names);
        if (rest.varCount() > 0) {
            prediction = prediction.bindVars(rest);
        }
        return prediction;
    }

    @Override
    public String toString() {
        return "PCA{}";
    }

    public String toSummary(Printer printer, POption<?>... options) {
        return "PCA decomposition\n"
                + "=================\n"
                + "input shape: rows=" + inputRows + ", vars=" + inputVars + "\n"
                + "eigen values:\n"
                + eigenValues.toSummary(printer, options) + "\n"
                + "Eigen vectors\n"
                + eigenVectors.toSummary(printer, options) + "\n";
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary(options);
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary(options);
    }
}
