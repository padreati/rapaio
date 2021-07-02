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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.EigenPair;
import rapaio.math.linear.Linear;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

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

    public final ValueParam<Double, PCA> eps = new ValueParam<>(this, 1e-100,
            "eps", "Fitting tolerance");
    public final ValueParam<Integer, PCA> maxRuns = new ValueParam<>(this, 2_000,
            "maxRuns", "Maximum number of iterations for fitting procedure", m -> m != null && m > 0);

    public final ValueParam<Boolean, PCA> center = new ValueParam<>(this, true,
            "scaling", "Flag which specifies if input scaling is applied.");

    public final ValueParam<Boolean, PCA> standardize = new ValueParam<>(this, false,
            "standardize", "Divide input by computed sample standard deviation.");

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
        EigenPair ep = Linear.eigenDecomp(s, maxRuns.get(), eps.get());
        eigenValues = ep.values().div(x.rowCount() - 1);
        eigenVectors = ep.vectors();

        logger.fine("sort eigen values and vectors");

        int[] mapping = IntArrays.newSeq(0, eigenValues.size());
        DoubleArrays.quickSortIndirect(mapping, eigenValues.asDense().elements(), 0, eigenValues.size());
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
        StringBuilder sb = new StringBuilder();
        sb.append("PCA decomposition\n");
        sb.append("=================\n");
        sb.append("input shape: rows=").append(inputRows).append(", vars=").append(inputVars).append("\n");
        sb.append("eigen values:\n");
        sb.append(eigenValues.toSummary(printer, options)).append("\n");
        sb.append("Eigen vectors\n");
        sb.append(eigenVectors.toSummary(printer, options)).append("\n");
        return sb.toString();
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
