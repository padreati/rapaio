/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import java.io.Serial;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.darray.DArray;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/15.
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

    protected DArray<Double> eigenValues;
    protected DArray<Double> eigenVectors;
    protected DArray<Double> mean;
    protected DArray<Double> sd;

    public DArray<Double> getValues() {
        return eigenValues;
    }

    public DArray<Double> getVectors() {
        return eigenVectors;
    }

    public DArray<Double> getMean() {
        return mean;
    }

    public DArray<Double> getStd() {
        return sd;
    }

    public PCA fit(Frame df) {
        preFit(df);

        logger.fine("start pca predict");
        DArray<Double> x = df.darray();
        logger.fine("compute mean, sd and do scaling");
        if (center.get()) {
            mean = x.mean1d(0);
            x.sub_(mean);
        }
        if (standardize.get()) {
            sd = x.std1d(0);
            x.div_(sd);
        }

        logger.fine("build scatter");
        DArray<Double> s = x.t().mm(x);

        logger.fine("compute eigenvalues");
        var evd = s.eig();
        eigenValues = evd.real().div(x.dim(0) - 1.);
        eigenVectors = evd.v();

        logger.fine("sort eigen values and vectors");

        int[] mapping = IntArrays.newSeq(0, eigenValues.size());
        eigenValues.externalSort(mapping, false);

        eigenValues = eigenValues.sel(0, mapping);
        eigenVectors = eigenVectors.sel(1, mapping);
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
        return transform("pca_", df, k);
    }

    /**
     * Transforms a given matrix into projections of the first k principal components.
     *
     * @param prefix prefix used for generated variables
     * @param df     initial data frame
     * @param k      number of principal components used
     * @return transformed input
     */
    public Frame transform(String prefix, Frame df, int k) {

        DArray<Double> x = df.mapVars(inputNames).darray();

        if (center.get()) {
            x.sub_(mean);
        }
        if (standardize.get()) {
            x.div_(sd);
        }

        String[] names = new String[k];
        for (int i = 0; i < k; i++) {
            names[i] = prefix + (i + 1);
        }

        DArray<Double> result = x.mm(eigenVectors.narrow(1, true, 0, k));

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

    public String toSummary(Printer printer, POpt<?>... options) {
        return """
                PCA decomposition
                =================
                input shape: rows=%d, vars=%s
                eigen values:
                %s
                eigen vectors:
                %s
                """.formatted(inputRows, inputVars, eigenValues.stretch(1).toContent(printer, options),
                eigenVectors.toContent(printer, options));
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(options);
    }
}
