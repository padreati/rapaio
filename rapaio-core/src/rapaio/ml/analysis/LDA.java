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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.data.stream.FSpot;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Linear discriminant analysis data transformation. This tool is similar with PCA, but it
 * projects the features on linear directions which separates estimated Gaussians class
 * distributions.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/5/15.
 */
public class LDA extends ParamSet<LDA> implements Printable {

    public static LDA newModel() {
        return new LDA();
    }

    private static final Logger logger = Logger.getLogger(LDA.class.getName());

    public final ValueParam<Boolean, LDA> scaling = new ValueParam<>(this, true, "Scaling data or not");

    protected String[] inputNames;
    private String targetName;
    private List<String> targetLevels;

    protected Tensor<Double> vmean;
    protected Tensor<Double> vstd;

    protected Tensor<Double> eigenValues;
    protected Tensor<Double> eigenVectors;

    private LDA() {
    }

    public Tensor<Double> eigenValues() {
        return eigenValues;
    }

    public Tensor<Double> eigenVectors() {
        return eigenVectors;
    }

    public void fit(Frame df, String targetVar) {
        validate(df, targetVar);

        logger.fine("start lda fit");
        Tensor<Double> mx = df.mapVars(inputNames).tensor();
        Tensor<Double> mxx = scaling.get() ? mx.bsub(0, mx.mean(0)).bdiv_(0, mx.std(0)) : mx;

        // compute global mean and std
        vmean = mxx.mean(0);
        vstd = mxx.std(0);

        // compute sliced data for each class
        Tensor<Double>[] mxxs = new Tensor[targetLevels.size()];
        for (int i = 0; i < targetLevels.size(); i++) {
            int index = i;
            mxxs[i] = mxx.take(0, df.stream()
                    .filter(s -> s.getLabel(targetName).equals(targetLevels.get(index)))
                    .mapToInt(FSpot::row)
                    .toArray());
        }

        // compute class means
        Tensor<Double>[] mcmeans = new Tensor[targetLevels.size()];
        for (int i = 0; i < targetLevels.size(); i++) {
            mcmeans[i] = mxxs[i].mean(0);
        }

        // build within scatter matrix

        Tensor<Double> xc = mxx.bsub(0, vmean).bdiv(0, vstd);
        Tensor<Double> sw = xc.t().mm(xc).div_((double) (xc.dim(0)));

        // build between-class scatter matrix

        Tensor<Double> mcmeansc = Tensors.stack(0, List.of(mcmeans)).bsub(0, vmean);
        Tensor<Double> sb = mcmeansc.t().mm(mcmeansc).div_((double) (mcmeansc.dim(0)));

        // inverse sw
        Tensor<Double> swi = sw.qr().inv();

        // use decomp of sbe
        var evd = sb.mm(swi).eig();


        logger.fine("compute eigenvalues");
        eigenValues = evd.real();
        eigenVectors = evd.v();

        logger.fine("sort eigen values and vectors");

        Integer[] rows = new Integer[eigenValues.size()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }

        Arrays.sort(rows, (o1, o2) -> -Double.compare(eigenValues.get(o1), eigenValues.get(o2)));
        int[] indexes = Arrays.stream(rows).mapToInt(v -> v).toArray();

        eigenValues = eigenValues.take(0, indexes).copy();
        eigenVectors = eigenVectors.take(1, indexes).copy();
    }

    /**
     * Transforms a given matrix into projections of the first k linear discriminant projections.
     *
     * @param df initial data frame
     * @param k  number of principal components used
     * @return transformed input
     */
    public Frame transform(Frame df, int k) {
        return transform("lda_", df, k);
    }

    /**
     * Transforms a given matrix into projections of the first k principal components.
     *
     * @param prefix prefix of the new variable names after transformation
     * @param df     initial data frame
     * @param k      number of principal components used
     * @return transformed input
     */
    public Frame transform(String prefix, Frame df, int k) {
        if (k <= 0 || k > targetLevels.size()) {
            throw new IllegalArgumentException("k must be a positive number less or equal with the number of levels.");
        }

        Tensor<Double> x = df.mapVars(inputNames).tensor();
        if (scaling.get()) {
            x = x.bsub(0, x.mean(0)).bdiv_(0, x.std(0));
        }

        if (targetLevels.size() < k) {
        }

        int[] dims = new int[k];
        String[] names = new String[k];
        for (int i = 0; i < dims.length; i++) {
            dims[i] = i;
            names[i] = prefix + (i + 1);
        }
        Tensor<Double> result = x.mm(eigenVectors.take(1, dims));
        Frame rest = df.removeVars(VarRange.of(inputNames));
        return rest.varCount() == 0 ?
                SolidFrame.matrix(result, names) :
                SolidFrame.matrix(result, names).bindVars(df.removeVars(VarRange.of(inputNames)));
    }

    private void validate(Frame df, String targetVar) {

        List<String> targetNames = VarRange.of(targetVar).parseVarNames(df);
        if (targetNames.size() > 1) {
            throw new IllegalArgumentException("LDA needs one target var");
        }
        targetName = targetNames.getFirst();
        Set<VarType> allowedTypes = new HashSet<>(Arrays.asList(VarType.BINARY, VarType.INT, VarType.DOUBLE));
        df.varStream().forEach(var -> {
            if (targetName.equals(var.name())) {
                if (!var.type().equals(VarType.NOMINAL)) {
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
        inputNames = df.varStream().map(Var::name).filter(name -> !name.equals(targetName)).toArray(String[]::new);
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();

        Frame eval = SolidFrame.byVars(
                VarDouble.empty(eigenValues.size()).name("values"),
                VarDouble.empty(eigenValues.size()).name("percent")
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
        sb.append(eval.toFullContent(printer, options)).append("\n");
        sb.append("Eigen vectors\n");
        sb.append("=============\n");
        sb.append(eigenVectors.toSummary(printer, options)).append("\n");

        return sb.toString();
    }
}
