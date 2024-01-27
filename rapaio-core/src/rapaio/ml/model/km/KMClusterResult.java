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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.model.km;

import java.util.HashMap;
import java.util.Map;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.tensor.Tensor;
import rapaio.ml.model.ClusteringResult;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/2/20.
 */
public class KMClusterResult extends ClusteringResult<KMCluster> {

    public static KMClusterResult valueOf(KMCluster model, Frame df, VarInt clusterAssignment) {
        return new KMClusterResult(model, df, clusterAssignment);
    }

    private final Frame clusterSummary;
    private final Var distances;

    private KMClusterResult(KMCluster model, Frame df, VarInt assignment) {
        super(model, df, assignment);

        Tensor<Double> c = model.getCentroidsMatrix();
        Tensor<Double> m = df.dtNew();
        int ccount = c.dim(0);

        Var id = VarInt.seq(1, ccount).name("ID");
        Var count = VarInt.fill(ccount, 0).name("count");
        Var mean = VarDouble.fill(ccount, 0).name("mean");
        Var variance = VarDouble.fill(ccount, 0).name("var");
        Var variancePercentage = VarDouble.fill(ccount, 0).name("var/total");
        Var std = VarDouble.fill(ccount, 0).name("sd");

        distances = VarDouble.empty().name("all dist");

        Map<Integer, VarDouble> errors = new HashMap<>();

        for (int i = 0; i < m.dim(0); i++) {
            double d = model.method.get().distance().compute(c.takesq(0, assignment.getInt(i)), m.takesq(0, i));
            errors.computeIfAbsent(assignment.getInt(i), row -> VarDouble.empty()).addDouble(d * d);
            distances.addDouble(d * d);
        }
        double totalVariance = Variance.of(distances).value();
        for (Map.Entry<Integer, VarDouble> e : errors.entrySet()) {
            count.setInt(e.getKey(), e.getValue().size());
            mean.setDouble(e.getKey(), Mean.of(e.getValue()).value());
            double v = Variance.of(e.getValue()).value();
            variance.setDouble(e.getKey(), v);
            variancePercentage.setDouble(e.getKey(), v / totalVariance);
            std.setDouble(e.getKey(), Math.sqrt(v));
        }
        clusterSummary = SolidFrame.byVars(id, count, mean, variance, variancePercentage, std);
    }

    public Frame getClusterSummary() {
        return clusterSummary;
    }

    public Var getDistances() {
        return distances;
    }

    @Override
    public String toString() {
        return "KMClusterResult{}";
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Overall errors: \n");
        sb.append("> count: ").append(distances.size()).append("\n");
        sb.append("> mean: ").append(Format.floatFlex(Mean.of(distances).value())).append("\n");
        sb.append("> var: ").append(Format.floatFlex(Variance.of(distances).value())).append("\n");
        sb.append("> sd: ").append(Format.floatFlex(Variance.of(distances).sdValue())).append("\n");
        sb.append("> inertia/error:").append(Format.floatFlex(model.getError())).append("\n");
        sb.append("> iterations:").append(model.getErrors().size()).append("\n");
        sb.append("\n");

        sb.append("Per cluster: \n");
        Frame sorted = clusterSummary.refSort(false, "count");
        sb.append(sorted.toFullContent(options));
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }
}
