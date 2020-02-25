package rapaio.ml.classifier.bayes.nb;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.format.Format;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorMLE implements Prior {

    private static final long serialVersionUID = 2590209274166763951L;

    private Map<String, Double> priors = new HashMap<>();

    @Override
    public Prior newInstance() {
        return new PriorMLE();
    }

    @Override
    public String name() {
        return "MLE";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append(priors.entrySet().stream().map(e -> e.getKey() + ":" + Format.floatFlex(e.getValue())).collect(Collectors.joining(",")));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void fitPriors(Frame df, Var weights, String targetVar) {
        priors = new HashMap<>();
        var target = df.rvar(targetVar);
        var dv = DensityVector.fromLevelWeights(false, target, weights);
        dv.normalize();
        for (String targetLevel : dv.index().getValues()) {
            priors.put(targetLevel, dv.get(targetLevel));
        }
    }

    @Override
    public double computePrior(String category) {
        return priors.getOrDefault(category, Double.NaN);
    }
}
