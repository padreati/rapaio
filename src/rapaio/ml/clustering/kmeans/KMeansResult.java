package rapaio.ml.clustering.kmeans;

import lombok.Getter;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.linear.DM;
import rapaio.math.linear.dense.DMStripe;
import rapaio.ml.clustering.ClusteringResult;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/2/20.
 */
@Getter
public class KMeansResult extends ClusteringResult {

    public static KMeansResult valueOf(KMeans model, Frame df, VarInt clusterAssignment) {
        return new KMeansResult(model, df, clusterAssignment);
    }

    private final KMeans kmeans;
    private final Frame clusterSummary;
    private final Var distances;

    private KMeansResult(KMeans model, Frame df, VarInt assignment) {
        super(model, df, assignment);
        this.kmeans = model;

        DM c = kmeans.getCentroidsMatrix();
        DM m = DMStripe.copy(df);
        int ccount = c.rowCount();

        Var id = VarInt.seq(1, ccount).withName("ID");
        Var count = VarInt.fill(ccount, 0).withName("count");
        Var mean = VarDouble.fill(ccount, 0).withName("mean");
        Var variance = VarDouble.fill(ccount, 0).withName("var");
        Var variancePercentage = VarDouble.fill(ccount, 0).withName("var/total");
        Var std = VarDouble.fill(ccount, 0).withName("sd");

        distances = VarDouble.empty().withName("all dist");

        Map<Integer, VarDouble> errors = new HashMap<>();

        for (int i = 0; i < m.rowCount(); i++) {
            double d = KMeans.distance(c.mapRow(assignment.getInt(i)), m.mapRow(i));
            errors.computeIfAbsent(assignment.getInt(i), row -> VarDouble.empty()).addDouble(d * d);
            distances.addDouble(d * d);
        }
        double totalVariance = Variance.of(distances).value();
        for (Map.Entry<Integer, VarDouble> e : errors.entrySet()) {
            count.setInt(e.getKey(), e.getValue().rowCount());
            mean.setDouble(e.getKey(), Mean.of(e.getValue()).value());
            double v = Variance.of(e.getValue()).value();
            variance.setDouble(e.getKey(), v);
            variancePercentage.setDouble(e.getKey(), v / totalVariance);
            std.setDouble(e.getKey(), Math.sqrt(v));
        }
        clusterSummary = SolidFrame.byVars(id, count, mean, variance, variancePercentage, std);
    }

    @Override
    public String toString() {
        return "KMeansResult{}";
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Overall: \n");
        sb.append("> count: ").append(distances.rowCount()).append("\n");
        sb.append("> mean: ").append(Format.floatFlex(Mean.of(distances).value())).append("\n");
        sb.append("> var: ").append(Format.floatFlex(Variance.of(distances).value())).append("\n");
        sb.append("> sd: ").append(Format.floatFlex(Variance.of(distances).sdValue())).append("\n");
        sb.append("> inertia:").append(Format.floatFlex(kmeans.getInertia())).append("\n");
        sb.append("> iterations:").append(kmeans.getErrors().rowCount()).append("\n");
        sb.append("\n");

        sb.append("Per cluster: \n");
        Frame sorted = clusterSummary.refSort(false, "count");
        sb.append(sorted.toFullContent(options));
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }
}
