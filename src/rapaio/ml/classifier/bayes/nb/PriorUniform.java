package rapaio.ml.classifier.bayes.nb;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.format.Format;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorUniform implements Prior {

    private static final long serialVersionUID = 4346918440758937122L;

    private Set<String> levels = new HashSet<>();
    private double uniformPrior = Double.NaN;

    @Override
    public Prior newInstance() {
        return new PriorUniform();
    }

    @Override
    public String name() {
        return "Uniform";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{value=").append(Format.floatFlex(uniformPrior)).append(",");
        sb.append("targetLevels=[").append(String.join(",", levels)).append("]");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void fitPriors(Frame df, Var weights, String targetVar) {
        List<String> targetLevels = new ArrayList<>(df.levels(targetVar));
        targetLevels = targetLevels.subList(1, targetLevels.size());
        if (!df.levels(targetVar).isEmpty()) {
            double degrees = df.levels(targetVar).size() - 1;
            uniformPrior = 1.0 / degrees;
            levels.addAll(targetLevels);
        }
    }

    @Override
    public double computePrior(String category) {
        if (levels.contains(category)) {
            return uniformPrior;
        }
        return Double.NaN;
    }
}
